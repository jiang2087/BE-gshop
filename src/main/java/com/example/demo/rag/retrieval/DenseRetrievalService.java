package com.example.demo.rag.retrieval;

import com.example.demo.rag.embbeding.EmbeddingService;
import com.example.demo.repository.products.ProductRepository;
import com.example.demo.utils.ProductUtil;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Common;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DenseRetrievalService {
    private static final int MAX_LIMIT = 100;
    private static final long ENTITY_CACHE_TTL_MS = 5 * 60 * 1000;
    private static final Set<String> LAPTOP_KEYWORDS = Set.of("laptop", "laptops", "notebook", "ultrabook", "macbook");
    private static final Set<String> MOBILE_KEYWORDS = Set.of("mobile", "mobiles", "phone", "phones", "smartphone", "smartphones", "dien thoai", "điện thoại");
    private static final Set<String> TELEVISION_KEYWORDS = Set.of("tv", "tvs", "television", "televisions", "tivi");
    private static final Set<String> WATCH_KEYWORDS = Set.of("watch", "watches", "smartwatch", "smartwatches", "dong ho", "đồng hồ");

    private final EmbeddingService embeddingService;
    private final QdrantClient qdrantClient;
    private final ProductRepository productRepository;
    private final ProductUtil productUtil;

    private volatile long entityCacheLoadedAt;
    private volatile List<String> cachedBrands = List.of();
    private volatile List<String> cachedNames = List.of();

    @Value("${qdrant.collection-name:product_variants}")
    private String collectionName;

    @Value("${qdrant.document-collection-name:knowledge_base}")
    private String docCollectionName;

    @Value("${retrieval.default-limit:10}")
    private int defaultLimit;

    @Value("${retrieval.default-score-threshold:0.0}")
    private float defaultScoreThreshold;

    @Value("${retrieval.hybrid.dense-weight:0.65}")
    private float denseWeight;

    @Value("${retrieval.hybrid.sparse-weight:0.25}")
    private float sparseWeight;

    @Value("${retrieval.hybrid.lexical-weight:0.10}")
    private float lexicalWeight;

    @Value("${retrieval.hybrid.keyword-candidate-limit:30}")
    private int keywordCandidateLimit;

    /**
     * Search for similar documents using dense retrieval
     * @param request Search request
     * @return Search response with results
     */
    public SearchResponse search(SearchRequest request) {
        if (request == null || request.getQuery() == null || request.getQuery().isBlank()) {
            throw new IllegalArgumentException("Search query cannot be null or empty");
        }

        long startTime = System.currentTimeMillis();
        String normalizedQuery = normalizeQuery(request.getQuery());
        ExtractedFilters extractedFilters = extractFilters(normalizedQuery);
        log.info("Starting dense retrieval search for query: {} (normalized: {}, filters: {})",
                request.getQuery(), normalizedQuery, extractedFilters);

        try {
            // Step 1: Generate query embedding
            List<Float> queryEmbedding = embeddingService.embed(normalizedQuery);
            log.debug("Generated query embedding with dimension: {}", queryEmbedding.size());

            // Step 2: Build search parameters
            int limit = request.getLimit() != null ? request.getLimit() : defaultLimit;
            if (limit <= 0) {
                limit = defaultLimit;
            }
            if (limit > MAX_LIMIT) {
                log.warn("Requested limit {} exceeds max {}, clamping", limit, MAX_LIMIT);
                limit = MAX_LIMIT;
            }
            float scoreThreshold = request.getScoreThreshold() != null ?
                    request.getScoreThreshold() : defaultScoreThreshold;
            if (scoreThreshold < 0f || scoreThreshold > 1f) {
                throw new IllegalArgumentException("Score threshold must be in range [0, 1]");
            }

            // Step 3: Execute search in both product and document collections
            Common.Filter productFilter = buildQdrantFilter(extractedFilters);
            List<ScoredPoint> productPoints = executeSearch(collectionName, queryEmbedding, limit, scoreThreshold, productFilter);
            List<ScoredPoint> scoredPoints = new ArrayList<>(productPoints);

            try {
                if (qdrantClient.collectionExistsAsync(docCollectionName).get()) {
                    List<ScoredPoint> docPoints = executeSearch(docCollectionName, queryEmbedding, limit, scoreThreshold, null);
                    scoredPoints.addAll(docPoints);
                }
            } catch (Exception ex) {
                log.warn("Document collection {} not available or search failed: {}", docCollectionName, ex.getMessage());
            }

            log.info("Found {} total results across collections for query", scoredPoints.size());

            // Step 4: Convert to search results
            List<SearchResult> results = scoredPoints.stream()
                    .map(this::convertToSearchResult)
                    .collect(Collectors.toList());
            results = rerankHybrid(normalizedQuery, results, limit);

            // Step 5: Build response
            long executionTime = System.currentTimeMillis() - startTime;
            Map<String, Object> appliedFilters = buildAppliedFiltersMap(scoreThreshold, extractedFilters);

            SearchResponse response = SearchResponse.builder()
                    .query(request.getQuery())
                    .results(results)
                    .totalResults(results.size())
                    .executionTimeMs(executionTime)
                    .appliedFilters(appliedFilters)
                    .truncated(results.size() >= limit)
                    .build();

            log.info("Search completed in {}ms with {} results", executionTime, results.size());
            return response;

        } catch (Exception e) {
            log.error("Failed to execute dense retrieval search", e);
            throw new RetrievalException("Failed to execute search", e);
        }
    }

    /**
     * Search with simple query string
     * @param query Query text
     * @return List of search results
     */
    public List<SearchResult> search(String query) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .limit(defaultLimit)
                .build();
        return search(request).getResults();
    }

    /**
     * Search with query and limit
     * @param query Query text
     * @param limit Maximum number of results
     * @return List of search results
     */
    public List<SearchResult> search(String query, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .limit(limit)
                .build();
        return search(request).getResults();
    }

    /**
     * Execute search against Qdrant
     */
    private List<ScoredPoint> executeSearch(
            String targetCollection,
            List<Float> queryEmbedding,
            int limit,
            float scoreThreshold,
            Common.Filter filter
    ) throws ExecutionException, InterruptedException {

        SearchPoints.Builder searchBuilder = SearchPoints.newBuilder()
                .setCollectionName(targetCollection)
                .addAllVector(queryEmbedding)
                .setLimit(limit)
                .setWithPayload(io.qdrant.client.WithPayloadSelectorFactory.enable(true));

        if (scoreThreshold > 0) {
            searchBuilder.setScoreThreshold(scoreThreshold);
        }

        if (filter != null) {
            searchBuilder.setFilter(filter);
        }

        return qdrantClient.searchAsync(searchBuilder.build()).get();
    }

    /**
     * Normalize query before embedding and sparse retrieval.
     */
    private String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }
        return query
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private ExtractedFilters extractFilters(String normalizedQuery) {
        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            return new ExtractedFilters(null, null, null);
        }

        refreshEntityCacheIfNeeded();

        String type = detectType(normalizedQuery);
        String brand = detectEntity(normalizedQuery, cachedBrands);
        String name = detectEntity(normalizedQuery, cachedNames);

        return new ExtractedFilters(type, brand, name);
    }

    private void refreshEntityCacheIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - entityCacheLoadedAt < ENTITY_CACHE_TTL_MS && !cachedBrands.isEmpty()) {
            return;
        }

        synchronized (this) {
            long recheck = System.currentTimeMillis();
            if (recheck - entityCacheLoadedAt < ENTITY_CACHE_TTL_MS && !cachedBrands.isEmpty()) {
                return;
            }

            cachedBrands = Optional.ofNullable(productRepository.findDistinctBrands()).orElseGet(List::of);
            cachedNames = Optional.ofNullable(productRepository.findDistinctNames()).orElseGet(List::of);
            entityCacheLoadedAt = recheck;
        }
    }

    private String detectType(String normalizedQuery) {
        if (containsAnyKeyword(normalizedQuery, LAPTOP_KEYWORDS)) {
            return "LAPTOP";
        }
        if (containsAnyKeyword(normalizedQuery, MOBILE_KEYWORDS)) {
            return "MOBILE";
        }
        if (containsAnyKeyword(normalizedQuery, TELEVISION_KEYWORDS)) {
            return "TELEVISION";
        }
        if (containsAnyKeyword(normalizedQuery, WATCH_KEYWORDS)) {
            return "WATCHES";
        }
        return null;
    }

    private boolean containsAnyKeyword(String query, Set<String> keywords) {
        String normalized = padForBoundary(normalizeEntityValue(query));
        for (String keyword : keywords) {
            if (normalized.contains(padForBoundary(normalizeEntityValue(keyword)))) {
                return true;
            }
        }
        return false;
    }

    private String detectEntity(String normalizedQuery, List<String> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        String normalized = padForBoundary(normalizeEntityValue(normalizedQuery));

        return candidates.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(candidate -> !candidate.isBlank())
                .sorted(Comparator.comparingInt(String::length).reversed())
                .filter(candidate -> normalized.contains(padForBoundary(normalizeEntityValue(candidate))))
                .findFirst()
                .orElse(null);
    }

    private String normalizeEntityValue(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String padForBoundary(String value) {
        return " " + value + " ";
    }

    private Common.Filter buildQdrantFilter(ExtractedFilters filters) {
        if (filters == null) {
            return null;
        }

        Common.Filter.Builder filterBuilder = Common.Filter.newBuilder();
        addKeywordCondition(filterBuilder, "type", filters.type());
        addKeywordCondition(filterBuilder, "brand", filters.brand());
        addKeywordCondition(filterBuilder, "name", filters.name());

        Common.Filter filter = filterBuilder.build();
        return filter.getMustCount() == 0 ? null : filter;
    }

    private void addKeywordCondition(Common.Filter.Builder filterBuilder, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        filterBuilder.addMust(
                Common.Condition.newBuilder()
                        .setField(
                                Common.FieldCondition.newBuilder()
                                        .setKey(key)
                                        .setMatch(
                                                Common.Match.newBuilder()
                                                        .setKeyword(value)
                                                        .build()
                                        )
                                        .build()
                        )
                        .build()
        );
    }

    /**
     * Hybrid reranking: dense score + sparse rank + lexical overlap.
     */
    private List<SearchResult> rerankHybrid(String normalizedQuery, List<SearchResult> denseResults, int limit) {
        if (denseResults.isEmpty()) {
            return denseResults;
        }

        int sparseLimit = Math.max(limit * 3, keywordCandidateLimit);
        Map<Long, Float> sparseScores = loadSparseProductScores(normalizedQuery, sparseLimit);
        float weightSum = sanitizeWeights(denseWeight, sparseWeight, lexicalWeight);

        float maxDense = denseResults.stream()
                .map(SearchResult::getScore)
                .filter(Objects::nonNull)
                .max(Float::compareTo)
                .orElse(1f);
        float minDense = denseResults.stream()
                .map(SearchResult::getScore)
                .filter(Objects::nonNull)
                .min(Float::compareTo)
                .orElse(0f);

        for (SearchResult result : denseResults) {
            float denseNormalized = normalizeScore(result.getScore(), minDense, maxDense);
            float sparseSignal = result.getProductId() == null ? 0f : sparseScores.getOrDefault(result.getProductId(), 0f);
            float lexicalSignal = lexicalOverlap(normalizedQuery, result);

            float hybridScore = (
                    denseWeight * denseNormalized +
                    sparseWeight * sparseSignal +
                    lexicalWeight * lexicalSignal
            ) / weightSum;

            if (result.getMetadata() == null) {
                result.setMetadata(new HashMap<>());
            }
            result.getMetadata().put("denseScoreRaw", result.getScore());
            result.getMetadata().put("denseScoreNormalized", denseNormalized);
            result.getMetadata().put("sparseScore", sparseSignal);
            result.getMetadata().put("lexicalScore", lexicalSignal);
            result.getMetadata().put("hybridScore", hybridScore);

            result.setScore(hybridScore);
        }

        return denseResults.stream()
                .sorted(Comparator.comparing(SearchResult::getScore, Comparator.nullsLast(Float::compareTo)).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Map<Long, Float> loadSparseProductScores(String normalizedQuery, int limit) {
        if (normalizedQuery.isBlank()) {
            return Collections.emptyMap();
        }

        String booleanKeyword = productUtil.toBooleanKeyword(normalizedQuery);
        Page<Long> page = productRepository.searchIds(booleanKeyword, PageRequest.of(0, limit));
        List<Long> ids = page.getContent();

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Float> scoreByProductId = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            Long productId = ids.get(i);
            if (productId == null) {
                continue;
            }
            // Reciprocal-rank style score in (0,1].
            float sparseScore = 1.0f / (i + 1);
            scoreByProductId.put(productId, sparseScore);
        }
        return scoreByProductId;
    }

    private float lexicalOverlap(String normalizedQuery, SearchResult result) {
        if (normalizedQuery.isBlank()) {
            return 0f;
        }

        Set<String> queryTerms = Arrays.stream(normalizedQuery.split("\\s+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toSet());
        if (queryTerms.isEmpty()) {
            return 0f;
        }

        String haystack = String.join(" ",
                Optional.ofNullable(result.getName()).orElse(""),
                Optional.ofNullable(result.getBrand()).orElse(""),
                Optional.ofNullable(result.getType()).orElse(""),
                Optional.ofNullable(result.getText()).orElse("")
        ).toLowerCase(Locale.ROOT);

        int hits = 0;
        for (String token : queryTerms) {
            if (haystack.contains(token)) {
                hits++;
            }
        }
        return (float) hits / queryTerms.size();
    }

    private float normalizeScore(Float score, float min, float max) {
        if (score == null) {
            return 0f;
        }
        float range = max - min;
        if (range <= 0f) {
            return 1f;
        }
        return (score - min) / range;
    }

    private float sanitizeWeights(float dense, float sparse, float lexical) {
        float sum = Math.max(0f, dense) + Math.max(0f, sparse) + Math.max(0f, lexical);
        return sum <= 0f ? 1f : sum;
    }

    /**
     * Convert Qdrant ScoredPoint to SearchResult
     */
    private SearchResult convertToSearchResult(ScoredPoint point) {
        Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payload = point.getPayloadMap();

        Long productId = extractLong(payload);
        Integer chunkIndex = extractInteger(payload, "chunkIndex");
        String text = extractString(payload, "text");
        String name = extractString(payload, "name");
        String brand = extractString(payload, "brand");
        String type = normalizeType(extractString(payload, "type"));
        Double minPrice = extractDouble(payload, "minPrice");
        Double maxPrice = extractDouble(payload, "maxPrice");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("tokenCount", extractInteger(payload, "tokenCount"));
        metadata.put("sentenceCount", extractInteger(payload, "sentenceCount"));

        return SearchResult.builder()
                .pointId(point.getId().getNum())
                .productId(productId)
                .chunkIndex(chunkIndex)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .score(point.getScore())
                .text(text)
                .name(name)
                .brand(brand)
                .type(type)
                .metadata(metadata)
                .build();
    }

    /**
     * Normalize type values from legacy payloads.
     */
    private String normalizeType(String type) {
        if (type == null) {
            return null;
        }

        String normalized = type.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "WATCH", "WATCHES" -> "WATCHES";
            case "LAPTOP", "LAPTOPS" -> "LAPTOP";
            case "MOBILE", "MOBILES", "PHONE", "PHONES" -> "MOBILE";
            case "TELEVISION", "TELEVISIONS", "TV", "TVS" -> "TELEVISION";
            default -> normalized;
        };
    }

    /**
     * Build applied filters map for response
     */
    private Map<String, Object> buildAppliedFiltersMap(float scoreThreshold, ExtractedFilters extractedFilters) {
        Map<String, Object> filters = new HashMap<>();
        filters.put("scoreThreshold", scoreThreshold);
        if (extractedFilters != null) {
            if (extractedFilters.type() != null) {
                filters.put("type", extractedFilters.type());
            }
            if (extractedFilters.brand() != null) {
                filters.put("brand", extractedFilters.brand());
            }
            if (extractedFilters.name() != null) {
                filters.put("name", extractedFilters.name());
            }
        }
        return filters;
    }

    /**
     * Extract Long value from Qdrant payload
     */
    private Long extractLong(Map<String, JsonWithInt.Value> payload) {
        if (payload.containsKey("productId")) {
            return payload.get("productId").getIntegerValue();
        }
        return null;
    }

    /**
     * Extract Integer value from Qdrant payload
     */
    private Integer extractInteger(Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payload, String key) {
        if (payload.containsKey(key)) {
            return (int) payload.get(key).getIntegerValue();
        }
        return null;
    }

    /**
     * Extract String value from Qdrant payload
     */
    private String extractString(Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payload, String key) {
        if (payload.containsKey(key)) {
            return payload.get(key).getStringValue();
        }
        return null;
    }

    /**
     * Extract Double value from Qdrant payload
     */
    private Double extractDouble(Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payload, String key) {
        if (payload.containsKey(key)) {
            return payload.get(key).getDoubleValue();
        }
        return null;
    }

    private record ExtractedFilters(String type, String brand, String name) {}
}