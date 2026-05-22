package com.example.demo.rag.ingestion;

import com.example.demo.dto.product.ProductDetailDto;
import com.example.demo.dto.product.ProductVariantDto;
import com.example.demo.rag.chunking.Chunk;
import com.example.demo.rag.chunking.ChunkingService;
import com.example.demo.rag.embbeding.EmbeddingService;
import com.example.demo.rag.semantic.SemanticBuilderService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Common;
import io.qdrant.client.grpc.Points.PointStruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

/**
 * Service for ingesting products into the vector database
 * Integrates natural-language building, chunking, and embedding
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final SemanticBuilderService semanticBuilderService;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final QdrantClient qdrantClient;

    @Value("${qdrant.collection-name:product_variants}")
    private String collectionName;


    public List<IngestionDocument> ingestProduct(ProductDetailDto product) {
        if (product == null) {
            log.warn("Null product provided for ingestion");
            return new ArrayList<>();
        }

        log.info("Starting ingestion for product: {} (ID: {})", product.name(), product.id());

        try {
            // Ensure stale chunks are removed before creating new natural-language chunks.
            if (product.id() != null) {
                qdrantClient.deleteAsync(collectionName, buildProductFilter(product.id())).get();
                log.info("Deleted existing vector points for product {}", product.id());
            }

            // Step 1: Build natural-language text
            String semanticText = buildSemanticTextForIngestion(product);
            log.debug("Generated natural-language text of length: {}", semanticText.length());

            // Step 2: Chunk natural-language text
            List<Chunk> chunks = chunkingService.chunkTextWithMetadata(semanticText);
            log.info("Created {} chunks for product {}", chunks.size(), product.id());

            // Step 3: Calculate price range
            PriceRange priceRange = calculatePriceRange(product);

            // Step 4: Generate embeddings and create documents
            List<IngestionDocument> documents = new ArrayList<>();
            List<PointStruct> points = new ArrayList<>();

            for (Chunk chunk : chunks) {
                // Generate embedding
                List<Float> embedding = embeddingService.embed(chunk.getContent());

                // Create document ID
                String docId = product.id() + "-" + chunk.getChunkIndex();
                long pointId = buildPointId(product.id(), chunk.getChunkIndex());

                // Create payload
                IngestionPayload payload = IngestionPayload.builder()
                        .productId(product.id())
                        .chunkIndex(chunk.getChunkIndex())
                        .text(chunk.getContent())
                        .name(product.name())
                        .brand(product.brand())
                        .type(product.productType())
                        .tokenCount(chunk.getTokenCount())
                        .sentenceCount(chunk.getSentenceCount())
                        .minPrice(priceRange.minPrice())
                        .maxPrice(priceRange.maxPrice())
                        .text(semanticText)
                        .build();

                // Create ingestion document
                IngestionDocument document = IngestionDocument.builder()
                        .id(docId)
                        .vector(embedding)
                        .payload(payload)
                        .build();

                documents.add(document);

                // Create Qdrant point
                PointStruct point = PointStruct.newBuilder()
                        .setId(id(pointId))
                        .setVectors(vectors(embedding))
                        .putAllPayload(createQdrantPayload(payload))
                        .build();

                points.add(point);

                log.debug("Created document {} with {} tokens", docId, chunk.getTokenCount());
            }

            // Step 5: Upsert to Qdrant
            if (!points.isEmpty()) {
                qdrantClient.upsertAsync(collectionName, points).get();
                log.info("Successfully ingested {} chunks for product {} into Qdrant", 
                        points.size(), product.id());
            }

            return documents;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Ingestion interrupted for product {}", product.id(), e);
            throw new IngestionException("Ingestion interrupted", e);
        } catch (ExecutionException e) {
            log.error("Failed to ingest product {}", product.id(), e);
            throw new IngestionException("Failed to ingest product", e);
        } catch (Exception e) {
            log.error("Unexpected error during ingestion for product {}", product.id(), e);
            throw new IngestionException("Unexpected ingestion error", e);
        }
    }

    /**
     * Ingest multiple products in batch
     * @param products List of products to ingest
     * @return List of all ingestion documents created
     */
    public List<IngestionDocument> ingestBatch(List<ProductDetailDto> products) {
        if (products == null || products.isEmpty()) {
            log.warn("Empty or null product list provided for batch ingestion");
            return new ArrayList<>();
        }

        log.info("Starting batch ingestion for {} products", products.size());

        List<IngestionDocument> allDocuments = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (ProductDetailDto product : products) {
            try {
                List<IngestionDocument> documents = ingestProduct(product);
                allDocuments.addAll(documents);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to ingest product {} in batch", product.id(), e);
                failureCount++;
            }
        }

        log.info("Batch ingestion completed: {} succeeded, {} failed, {} total documents",
                successCount, failureCount, allDocuments.size());

        return allDocuments;
    }

    /**
     * Delete a product from the vector database
     * @param productId Product ID to delete
     */
    public void deleteProduct(Long productId) {
        if (productId == null) {
            log.warn("Null product ID provided for deletion");
            return;
        }

        try {
            log.info("Deleting product {} from vector database", productId);

            qdrantClient.deleteAsync(collectionName, buildProductFilter(productId)).get();
            log.info("Deleted vector points for product {}", productId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Delete interrupted for product {}", productId, e);
            throw new IngestionException("Delete interrupted", e);
        } catch (ExecutionException e) {
            log.error("Failed to delete product {} from vector database", productId, e);
            throw new IngestionException("Failed to delete product from vector database", e);
        } catch (Exception e) {
            log.error("Failed to delete product {}", productId, e);
            throw new IngestionException("Failed to delete product", e);
        }
    }

    /**
     * Delete all points from configured collection
     */
    public void clearCollection() {
        clearCollection(collectionName);
    }

    /**
     * Delete all points from target collection
     * @param targetCollection Collection name
     */
    public void clearCollection(String targetCollection) {
        if (targetCollection == null || targetCollection.isBlank()) {
            log.warn("Blank collection name provided for clear operation");
            return;
        }

        try {
            log.info("Clearing all vector points from collection {}", targetCollection);
            qdrantClient.deleteAsync(targetCollection, buildMatchAllFilter()).get();
            log.info("Cleared all vector points from collection {}", targetCollection);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Clear collection interrupted for {}", targetCollection, e);
            throw new IngestionException("Clear collection interrupted", e);
        } catch (ExecutionException e) {
            log.error("Failed to clear collection {}", targetCollection, e);
            throw new IngestionException("Failed to clear collection", e);
        } catch (Exception e) {
            log.error("Unexpected error while clearing collection {}", targetCollection, e);
            throw new IngestionException("Unexpected clear collection error", e);
        }
    }

    /**
     * Get ingestion statistics
     * @param product Product to analyze
     * @return Ingestion statistics
     */
    public IngestionStats getIngestionStats(ProductDetailDto product) {
        if (product == null) {
            return new IngestionStats(0, 0, 0, 0, 0);
        }

        String semanticText = buildSemanticTextForIngestion(product);
        List<Chunk> chunks = chunkingService.chunkTextWithMetadata(semanticText);

        int totalChunks = chunks.size();
        int totalTokens = chunks.stream().mapToInt(Chunk::getTokenCount).sum();
        int avgTokensPerChunk = totalChunks > 0 ? totalTokens / totalChunks : 0;
        int embeddingDimension = embeddingService.getDimension();
        int estimatedVectorSize = totalChunks * embeddingDimension * 4; // 4 bytes per float

        return new IngestionStats(
                totalChunks,
                totalTokens,
                avgTokensPerChunk,
                embeddingDimension,
                estimatedVectorSize
        );
    }

    /**
     * Calculate min and max price from product variants
     * @param product Product to analyze
     * @return PriceRange containing min and max prices
     */
    private PriceRange calculatePriceRange(ProductDetailDto product) {
        if (product.productVariants() == null || product.productVariants().isEmpty()) {
            return new PriceRange(null, null);
        }

        Double minPrice = null;
        Double maxPrice = null;

        for (ProductVariantDto variant : product.productVariants()) {
            Double price = variant.discountedPrice() != null ? variant.discountedPrice() : variant.originalPrice();
            
            if (price != null) {
                if (minPrice == null || price < minPrice) {
                    minPrice = price;
                }
                if (maxPrice == null || price > maxPrice) {
                    maxPrice = price;
                }
            }
        }

        return new PriceRange(minPrice, maxPrice);
    }

    private String buildSemanticTextForIngestion(ProductDetailDto product) {
        String semanticText = semanticBuilderService.buildSemanticText(product);
        if (semanticText != null && !semanticText.isBlank()) {
            return semanticText;
        }

        StringBuilder fallback = new StringBuilder();
        if (product.name() != null && !product.name().isBlank()) {
            fallback.append(product.name());
        }
        if (product.brand() != null && !product.brand().isBlank()) {
            if (!fallback.isEmpty()) {
                fallback.append(". ");
            }
            fallback.append("Brand: ").append(product.brand());
        }
        if (product.productType() != null && !product.productType().isBlank()) {
            if (!fallback.isEmpty()) {
                fallback.append(". ");
            }
            fallback.append("Type: ").append(product.productType().trim().toUpperCase(Locale.ROOT));
        }
        if (product.description() != null && !product.description().isBlank()) {
            if (!fallback.isEmpty()) {
                fallback.append(". ");
            }
            fallback.append(product.description());
        }

        if (product.productAttributes() != null && !product.productAttributes().isEmpty()) {
            product.productAttributes().forEach((key, value) -> {
                if (key != null && !key.isBlank() && value != null && !value.toString().isBlank()) {
                    fallback.append(". ").append(key).append(": ").append(value);
                }
            });
        }

        if (!fallback.isEmpty()) {
            log.warn("Natural-language text empty for product {}, using ingestion fallback text", product.id());
            return fallback.toString();
        }

        throw new IngestionException("Cannot build natural-language text for product " + product.id());
    }

    /**
     * Convert payload to Qdrant-compatible map
     */
    private Map<String, io.qdrant.client.grpc.JsonWithInt.Value> createQdrantPayload(IngestionPayload payload) {
        Map<String, io.qdrant.client.grpc.JsonWithInt.Value> map = new HashMap<>();
        
        map.put("productId", value(payload.getProductId()));
        map.put("chunkIndex", value(payload.getChunkIndex()));
        map.put("text", value(payload.getText()));
        map.put("name", value(payload.getName()));
        map.put("brand", value(payload.getBrand()));
        map.put("type", value(payload.getType()));
        
        if (payload.getTokenCount() != null) {
            map.put("tokenCount", value(payload.getTokenCount()));
        }
        if (payload.getSentenceCount() != null) {
            map.put("sentenceCount", value(payload.getSentenceCount()));
        }
        if (payload.getMinPrice() != null) {
            map.put("minPrice", value(payload.getMinPrice()));
        }
        if (payload.getMaxPrice() != null) {
            map.put("maxPrice", value(payload.getMaxPrice()));
        }
        
        return map;
    }

    /**
     * Build deterministic numeric point id for Qdrant.
     * Avoid parsing "productId-chunkIndex" string, which is not numeric.
     */
    private long buildPointId(Long productId, Integer chunkIndex) {
        if (productId == null || chunkIndex == null) {
            throw new IngestionException("Product ID and chunk index must not be null");
        }

        try {
            return Math.addExact(Math.multiplyExact(productId, 1_000_000L), chunkIndex);
        } catch (ArithmeticException ex) {
            // Fallback if multiplication overflows for very large IDs.
            return Integer.toUnsignedLong((productId + "-" + chunkIndex).hashCode());
        }
    }

    /**
     * Extract basic description from natural-language text
     * Returns only the product description part, excluding use cases and categories
     */
    private String extractBasicDescription(String semanticText) {
        if (semanticText == null || semanticText.isBlank()) {
            return semanticText;
        }
        
        // Find the first occurrence of section markers
        int bestForIndex = semanticText.indexOf("\nBest for:");
        int categoryIndex = semanticText.indexOf("\nCategory:");
        
        // Determine where to cut
        int cutIndex = -1;
        if (bestForIndex != -1 && categoryIndex != -1) {
            cutIndex = Math.min(bestForIndex, categoryIndex);
        } else if (bestForIndex != -1) {
            cutIndex = bestForIndex;
        } else if (categoryIndex != -1) {
            cutIndex = categoryIndex;
        }
        
        // Extract basic description
        if (cutIndex != -1) {
            return semanticText.substring(0, cutIndex).trim();
        }
        
        return semanticText.trim();
    }
    private Common.Filter buildProductFilter(Long productId) {
        return Common.Filter.newBuilder()
                .addMust(Common.Condition.newBuilder()
                        .setField(Common.FieldCondition.newBuilder()
                                .setKey("productId")
                                .setMatch(Common.Match.newBuilder()
                                        .setInteger(productId)
                                        .build())
                                .build())
                        .build())
                .build();
    }

    private Common.Filter buildMatchAllFilter() {
        return Common.Filter.newBuilder().build();
    }

    /**
     * Price range record
     */
    private record PriceRange(Double minPrice, Double maxPrice) {}

    /**
     * Statistics about ingestion
     */
    @Getter
    public static class IngestionStats {
        private final int totalChunks;
        private final int totalTokens;
        private final int avgTokensPerChunk;
        private final int embeddingDimension;
        private final int estimatedVectorSizeBytes;

        public IngestionStats(
                int totalChunks,
                int totalTokens,
                int avgTokensPerChunk,
                int embeddingDimension,
                int estimatedVectorSizeBytes
        ) {
            this.totalChunks = totalChunks;
            this.totalTokens = totalTokens;
            this.avgTokensPerChunk = avgTokensPerChunk;
            this.embeddingDimension = embeddingDimension;
            this.estimatedVectorSizeBytes = estimatedVectorSizeBytes;
        }

    }
}
