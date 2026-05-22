package com.example.demo.services.ai;

import com.example.demo.dto.product.ProductDetailDto;
import com.example.demo.models.Product;
import com.example.demo.rag.embbeding.EmbeddingService;
import com.example.demo.repository.products.ProductRepository;
import com.example.demo.services.products.ProductVariantService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.WithPayloadSelectorFactory;
import io.qdrant.client.grpc.Common;
import io.qdrant.client.grpc.Points.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class QdrantSearchService {

    private final QdrantClient qdrantClient;
    private final EmbeddingService embeddingService;
    private final ProductRepository productRepository;
    private final ProductVariantService productVariantService;

    @Value("${qdrant.collection-name:product_variants}")
    private String collectionName;


    /**
     * Search products by text query using semantic similarity
     * @param query Text query to search for
     * @param limit Maximum number of results to return
     * @return List of matching products ordered by relevance
     */
    public List<Product> searchByText(String query, Integer limit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be null or empty");
        }

        int searchLimit = (limit != null && limit > 0) ? limit : 10;
        log.info("=== START: Searching products by text query: '{}' with limit: {} ===", query, searchLimit);

        try {
            // Convert text query to embedding vector
            log.debug("Generating embedding for query: '{}'", query);
            List<Float> queryVector = embeddingService.embed(query);
            
            if (queryVector == null || queryVector.isEmpty()) {
                log.error("Failed to generate embedding for query: '{}'", query);
                throw new RuntimeException("Failed to generate embedding for search query");
            }
            
            log.debug("Generated embedding vector with dimension: {}", queryVector.size());

            // Search Qdrant using the query vector
            List<ScoredPoint> searchResults = searchSimilarByVector(queryVector, searchLimit, null);
            log.debug("Qdrant returned {} results for text query", searchResults.size());

            if (searchResults.isEmpty()) {
                log.warn("No results found for query: '{}'", query);
                return Collections.emptyList();
            }

            // Extract product IDs from search results
            Set<Long> productIds = new LinkedHashSet<>();
            for (ScoredPoint point : searchResults) {
                Long productId = extractProductId(point.getPayloadMap());
                if (productId != null) {
                    log.debug("Found productId: {} with score: {}", productId, point.getScore());
                    productIds.add(productId);
                }
            }

            if (productIds.isEmpty()) {
                log.warn("No valid product IDs extracted from search results");
                return Collections.emptyList();
            }

            // Fetch products from database
            List<Product> products = productRepository.findAllById(productIds);
            log.debug("Database returned {} products", products.size());

            // Maintain order from search results
            Map<Long, Integer> orderMap = new HashMap<>();
            int index = 0;
            for (Long id : productIds) {
                orderMap.put(id, index++);
            }
            products.sort(Comparator.comparing(p -> orderMap.getOrDefault(p.getId(), Integer.MAX_VALUE)));

            log.info("=== SUCCESS: Found {} products for query: '{}' ===", products.size(), query);
            return products;

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to search products by text: '{}'", query, e);
            throw new RuntimeException("Failed to search products: " + e.getMessage(), e);
        }
    }


    public List<Product> searchByTextWithThreshold(String query, Integer limit, Float scoreThreshold) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query cannot be null or empty");
        }

        int searchLimit = (limit != null && limit > 0) ? limit : 10;
        float threshold = (scoreThreshold != null && scoreThreshold > 0) ? scoreThreshold : 0.0f;
        
        log.info("=== START: Searching products by text: '{}' with limit: {} and threshold: {} ===", 
                query, searchLimit, threshold);

        try {
            // Convert text query to embedding vector
            List<Float> queryVector = embeddingService.embed(query);
            
            if (queryVector == null || queryVector.isEmpty()) {
                log.error("Failed to generate embedding for query: '{}'", query);
                throw new RuntimeException("Failed to generate embedding for search query");
            }

            // Search Qdrant using the query vector
            List<ScoredPoint> searchResults = searchSimilarByVector(queryVector, searchLimit, null);
            
            // Filter by score threshold
            Set<Long> productIds = new LinkedHashSet<>();
            for (ScoredPoint point : searchResults) {
                if (point.getScore() >= threshold) {
                    Long productId = extractProductId(point.getPayloadMap());
                    if (productId != null) {
                        log.debug("Found productId: {} with score: {} (threshold: {})", 
                                productId, point.getScore(), threshold);
                        productIds.add(productId);
                    }
                } else {
                    log.debug("Filtered out result with score: {} (below threshold: {})", 
                            point.getScore(), threshold);
                }
            }

            if (productIds.isEmpty()) {
                log.warn("No products found matching query: '{}' with threshold: {}", query, threshold);
                return Collections.emptyList();
            }

            // Fetch and order products
            List<Product> products = productRepository.findAllById(productIds);
            Map<Long, Integer> orderMap = new HashMap<>();
            int index = 0;
            for (Long id : productIds) {
                orderMap.put(id, index++);
            }
            products.sort(Comparator.comparing(p -> orderMap.getOrDefault(p.getId(), Integer.MAX_VALUE)));

            log.info("=== SUCCESS: Found {} products for query: '{}' with threshold: {} ===", 
                    products.size(), query, threshold);
            return products;

        } catch (Exception e) {
            log.error("Failed to search products by text with threshold: '{}'", query, e);
            throw new RuntimeException("Failed to search products: " + e.getMessage(), e);
        }
    }

    public List<ProductDetailDto> findSimilarProducts(
            Long productId,
            int limit
    ) {

        try {
            Long pointId = findPointIdByProductId(productId);
            if (pointId == null) {

                log.warn(
                        "No Qdrant point found for productId={}",
                        productId
                );
                return Collections.emptyList();
            }

            RecommendPoints recommendRequest =
                    RecommendPoints.newBuilder()
                            .setCollectionName(collectionName)
                            .addPositive(
                                    Common.PointId.newBuilder()
                                            .setNum(pointId)
                                            .build()
                            )
                            .setLimit(limit + 1)
                            .setWithPayload(
                                    WithPayloadSelectorFactory.enable(true)
                            )
                            .build();

            List<ScoredPoint> scoredPoints =
                    qdrantClient
                            .recommendAsync(recommendRequest)
                            .get();

            if (scoredPoints.isEmpty()) {
                return Collections.emptyList();
            }



            LinkedHashSet<Long> productIds =
                    new LinkedHashSet<>();

            for (ScoredPoint point : scoredPoints) {

                Long similarProductId =
                        extractProductId(
                                point.getPayloadMap()
                        );

                if (similarProductId == null) {
                    continue;
                }


                if (similarProductId.equals(productId)) {
                    continue;
                }

                // optional threshold
                if (point.getScore() < 0.45f) {
                    continue;
                }

                productIds.add(similarProductId);

                if (productIds.size() >= limit) {
                    break;
                }
            }

            if (productIds.isEmpty()) {
                return Collections.emptyList();
            }

            Map<Long, ProductDetailDto> productsMap =
                    productVariantService.getProductsByIds(new ArrayList<>(productIds));

            List<ProductDetailDto> products = new ArrayList<>();
            for (Long id : productIds) {
                ProductDetailDto dto = productsMap.get(id);
                if (dto != null) {
                    products.add(dto);
                }
            }

            return products;

        } catch (Exception e) {

            log.error(
                    "Failed to find similar products",
                    e
            );

            return Collections.emptyList();
        }
    }

    private Long findPointIdByProductId(Long productId)
            throws Exception {

        Common.Filter filter = Common.Filter.newBuilder()
                .addMust(
                        Common.Condition.newBuilder()
                                .setField(
                                        Common.FieldCondition.newBuilder()
                                                .setKey("productId")
                                                .setMatch(
                                                        Common.Match.newBuilder()
                                                                .setInteger(productId)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        ScrollPoints scroll = ScrollPoints.newBuilder()
                .setCollectionName(collectionName)
                .setFilter(filter)
                .setLimit(1)
                .build();

        ScrollResponse response =
                qdrantClient.scrollAsync(scroll).get();

        if (response.getResultList().isEmpty()) {
            return null;
        }

        return response
                .getResultList()
                .getFirst()
                .getId()
                .getNum();
    }

    private List<Float> getProductEmbedding(Long productId) throws ExecutionException, InterruptedException {
        Common.Filter filter = Common.Filter.newBuilder()
                .addMust(Common.Condition.newBuilder()
                        .setField(Common.FieldCondition.newBuilder()
                                .setKey("productId")
                                .setMatch(Common.Match.newBuilder()
                                        .setInteger(productId)
                                        .build())
                                .build())
                        .build())
                .build();

        ScrollPoints scrollRequest = ScrollPoints.newBuilder()
                .setCollectionName(collectionName)
                .setFilter(filter)
                .setLimit(1)
                .setWithVectors(WithVectorsSelector.newBuilder()
                        .setEnable(true)
                        .build())
                .build();

        ScrollResponse scrollResponse = qdrantClient.scrollAsync(scrollRequest).get();
        List<RetrievedPoint> points = scrollResponse.getResultList();

        if (points.isEmpty()) {
            log.warn("No points found in Qdrant for productId: {} in collection: {}", productId, collectionName);
            return Collections.emptyList();
        }

        RetrievedPoint point = points.getFirst();
        List<Float> vector = point.getVectors().getVector().getDataList();
        return vector;
    }

    private List<ScoredPoint> searchSimilarByVector(List<Float> vector, int limit, Long filterProductId)
            throws ExecutionException, InterruptedException {

        SearchPoints.Builder searchBuilder = SearchPoints.newBuilder()
                .setCollectionName(collectionName)
                .addAllVector(vector)
                .setLimit(limit)
                .setWithPayload(
                        WithPayloadSelectorFactory.enable(true)
                );

        if (filterProductId != null) {
            Common.Filter filter = Common.Filter.newBuilder()
                    .addMust(Common.Condition.newBuilder()
                            .setField(Common.FieldCondition.newBuilder()
                                    .setKey("productId")
                                    .setMatch(Common.Match.newBuilder()
                                            .setInteger(filterProductId)
                                            .build())
                                    .build())
                            .build())
                    .build();
            searchBuilder.setFilter(filter);
            log.debug("Applying productId filter: {}", filterProductId);
        }

        return qdrantClient.searchAsync(searchBuilder.build()).get();
    }

    private Long extractProductId(Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payload) {
        if (payload.containsKey("productId")) {
            return payload.get("productId").getIntegerValue();
        }
        log.warn("Payload does not contain 'productId' key. Available keys: {}", payload.keySet());
        return null;
    }
}
