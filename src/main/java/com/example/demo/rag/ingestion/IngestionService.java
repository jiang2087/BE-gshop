package com.example.demo.rag.ingestion;

import com.example.demo.dto.product.ProductDetailDto;
import com.example.demo.rag.chunking.Chunk;
import com.example.demo.rag.chunking.ChunkingService;
import com.example.demo.rag.embbeding.EmbeddingService;
import com.example.demo.rag.semantic.SemanticBuilderService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points.PointStruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

/**
 * Service for ingesting products into the vector database
 * Integrates semantic building, chunking, and embedding
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

    /**
     * Ingest a single product into the vector database
     * @param product Product to ingest
     * @return List of ingestion documents created
     */
    public List<IngestionDocument> ingestProduct(ProductDetailDto product) {
        if (product == null) {
            log.warn("Null product provided for ingestion");
            return new ArrayList<>();
        }

        log.info("Starting ingestion for product: {} (ID: {})", product.name(), product.id());

        try {
            // Step 1: Build semantic text
            String semanticText = semanticBuilderService.buildSemanticText(product);
            log.debug("Generated semantic text of length: {}", semanticText.length());

            // Step 2: Chunk the semantic text
            List<Chunk> chunks = chunkingService.chunkTextWithMetadata(semanticText);
            log.info("Created {} chunks for product {}", chunks.size(), product.id());

            // Step 3: Generate embeddings and create documents
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

            // Step 4: Upsert to Qdrant
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
            throw new IngestionException("Unexpected error during ingestion", e);
        }
    }

    /**
     * Ingest multiple products in batch
     * @param products List of products to ingest
     * @return List of all ingestion documents created
     */
    public List<IngestionDocument> ingestBatch(List<ProductDetailDto> products) {
        if (products == null || products.isEmpty()) {
            log.warn("Empty product list provided for batch ingestion");
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
            // Delete all chunks for this product
            // Qdrant filter: productId == productId
            log.info("Deleting product {} from vector database", productId);
            
            // Note: This requires implementing a filter-based delete
            // For now, we'll log a warning
            log.warn("Delete by filter not implemented. Manual cleanup required for product {}", productId);
            
        } catch (Exception e) {
            log.error("Failed to delete product {}", productId, e);
            throw new IngestionException("Failed to delete product", e);
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

        String semanticText = semanticBuilderService.buildSemanticText(product);
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
