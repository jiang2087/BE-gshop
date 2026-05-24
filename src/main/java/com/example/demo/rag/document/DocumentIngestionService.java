package com.example.demo.rag.document;

import com.example.demo.rag.chunking.Chunk;
import com.example.demo.rag.chunking.ChunkingService;
import com.example.demo.rag.embbeding.EmbeddingService;
import com.example.demo.rag.ingestion.IngestionException;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Points.PointStruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final QdrantClient qdrantClient;

    @Value("${qdrant.document-collection-name:knowledge_base}")
    private String docCollectionName;

    @Async
    public CompletableFuture<Void> ingestDocument(String traceId, String title, String content, String sourceType) {
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            throw new IllegalArgumentException("Title and content must not be null or blank");
        }

        log.info("[RAG_DOC_PIPELINE][{}][ASYNC_START] title={} sourceType={} collection={}",
                traceId, title, sourceType, docCollectionName);

        try {
            ensureCollectionExists(traceId);

            List<Chunk> chunks = chunkingService.chunkTextWithMetadata(content);
            log.info("[RAG_DOC_PIPELINE][{}][CHUNK_OK] title={} chunks={}", traceId, title, chunks.size());

            if (chunks.isEmpty()) {
                log.warn("[RAG_DOC_PIPELINE][{}][CHUNK_EMPTY] title={}", traceId, title);
                return CompletableFuture.completedFuture(null);
            }

            List<String> chunkContents = chunks.stream()
                    .map(Chunk::getContent)
                    .toList();

            log.info("[RAG_DOC_PIPELINE][{}][EMBED_START] title={} chunkCount={}",
                    traceId, title, chunkContents.size());
            List<List<Float>> embeddings = embeddingService.embedBatch(chunkContents);
            log.info("[RAG_DOC_PIPELINE][{}][EMBED_OK] title={} vectors={}",
                    traceId, title, embeddings.size());

            List<PointStruct> points = new ArrayList<>();
            int skippedEmbeddings = 0;
            for (int i = 0; i < chunks.size(); i++) {
                Chunk chunk = chunks.get(i);
                List<Float> embedding = i < embeddings.size() ? embeddings.get(i) : null;

                if (embedding == null || embedding.isEmpty()) {
                    skippedEmbeddings++;
                    log.warn("[RAG_DOC_PIPELINE][{}][EMBED_EMPTY] title={} chunkIndex={}",
                            traceId, title, i);
                    continue;
                }

                long pointId = buildPointId(title, chunk.getChunkIndex());

                Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payloadMap = new HashMap<>();
                payloadMap.put("text", value(chunk.getContent()));
                payloadMap.put("name", value(title));
                payloadMap.put("brand", value("DOCUMENT"));
                payloadMap.put("type", value(sourceType));
                payloadMap.put("chunkIndex", value(chunk.getChunkIndex()));
                payloadMap.put("tokenCount", value(chunk.getTokenCount()));
                payloadMap.put("sentenceCount", value(chunk.getSentenceCount()));

                PointStruct point = PointStruct.newBuilder()
                        .setId(id(pointId))
                        .setVectors(vectors(embedding))
                        .putAllPayload(payloadMap)
                        .build();

                points.add(point);
            }

            log.info("[RAG_DOC_PIPELINE][{}][POINT_BUILD_OK] title={} points={} skippedEmbeddings={}",
                    traceId, title, points.size(), skippedEmbeddings);

            if (!points.isEmpty()) {
                log.info("[RAG_DOC_PIPELINE][{}][UPSERT_START] title={} collection={} points={}",
                        traceId, title, docCollectionName, points.size());
                qdrantClient.upsertAsync(docCollectionName, points).get();
                log.info("[RAG_DOC_PIPELINE][{}][UPSERT_OK] title={} points={} collection={}",
                        traceId, title, points.size(), docCollectionName);
            } else {
                log.warn("[RAG_DOC_PIPELINE][{}][UPSERT_SKIPPED] title={} reason=no_valid_points",
                        traceId, title);
            }

            log.info("[RAG_DOC_PIPELINE][{}][PIPELINE_DONE] title={}", traceId, title);
            return CompletableFuture.completedFuture(null);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[RAG_DOC_PIPELINE][{}][PIPELINE_INTERRUPTED] title={}", traceId, title, e);
            throw new IngestionException("Ingestion interrupted", e);
        } catch (ExecutionException e) {
            log.error("[RAG_DOC_PIPELINE][{}][PIPELINE_EXECUTION_FAILED] title={}", traceId, title, e);
            throw new IngestionException("Failed to ingest document", e);
        } catch (Exception e) {
            log.error("[RAG_DOC_PIPELINE][{}][PIPELINE_UNEXPECTED_FAILED] title={}", traceId, title, e);
            throw new IngestionException("Unexpected ingestion error", e);
        }
    }

    private void ensureCollectionExists(String traceId) throws ExecutionException, InterruptedException {
        boolean collectionExists = qdrantClient.collectionExistsAsync(docCollectionName).get();
        if (!collectionExists) {
            int dimension = embeddingService.getDimension();
            log.info("[RAG_DOC_PIPELINE][{}][COLLECTION_CREATE_START] collection={} dimension={} distance=Cosine",
                    traceId, docCollectionName, dimension);

            VectorParams vectorParams = VectorParams.newBuilder()
                    .setSize(dimension)
                    .setDistance(Distance.Cosine)
                    .build();

            qdrantClient.createCollectionAsync(docCollectionName, vectorParams).get();
            log.info("[RAG_DOC_PIPELINE][{}][COLLECTION_CREATE_OK] collection={}", traceId, docCollectionName);
        } else {
            log.info("[RAG_DOC_PIPELINE][{}][COLLECTION_EXISTS] collection={}", traceId, docCollectionName);
        }
    }

    private long buildPointId(String title, Integer chunkIndex) {
        if (title == null || chunkIndex == null) {
            throw new IngestionException("Title and chunk index must not be null");
        }
        long hash = Integer.toUnsignedLong(title.hashCode());
        try {
            return Math.addExact(Math.multiplyExact(hash, 1000L), chunkIndex);
        } catch (ArithmeticException ex) {
            return Integer.toUnsignedLong((title + "-" + chunkIndex).hashCode());
        }
    }
}
