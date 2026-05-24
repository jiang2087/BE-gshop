package com.example.demo.rag.embbeding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class OllamaEmbeddingService implements EmbeddingService {

    /**
     * Số chunk tối đa gửi trong một lần gọi Ollama /api/embed
     * Ollama hỗ trợ nhận List<String> input nên nhiều chunk = ít round-trip hơn.
     */
    private static final int DEFAULT_BATCH_SIZE = 32;

    /**
     * Giới hạn số batch gọi đồng thời lên Ollama để tránh quá tải.
     */
    private static final int DEFAULT_MAX_CONCURRENT_BATCHES = 2;

    private final WebClient webClient;
    private final String model;
    private final int dimension;
    private final int batchSize;
    private final int maxConcurrentBatches;
    private final Duration singleTimeout;
    private final Duration batchTimeout;
    private final long singleRetryCount;
    private final long batchRetryCount;

    public OllamaEmbeddingService(
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.embedding.model:nomic-embed-text}") String model,
            @Value("${ollama.embedding.dimension:768}") int dimension,
            @Value("${ollama.embedding.max-in-memory-size:16777216}") int maxInMemorySize,
            @Value("${ollama.embedding.single-timeout-seconds:30}") long singleTimeoutSeconds,
            @Value("${ollama.embedding.batch-timeout-seconds:180}") long batchTimeoutSeconds,
            @Value("${ollama.embedding.single-retry-count:2}") long singleRetryCount,
            @Value("${ollama.embedding.batch-retry-count:1}") long batchRetryCount,
            @Value("${ollama.embedding.batch-size:32}") int configuredBatchSize,
            @Value("${ollama.embedding.max-concurrent-batches:2}") int configuredMaxConcurrentBatches
    ) {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(maxInMemorySize))
                .build();

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(exchangeStrategies)
                .build();
        this.model = model;
        this.dimension = dimension;
        this.batchSize = configuredBatchSize > 0 ? configuredBatchSize : DEFAULT_BATCH_SIZE;
        this.maxConcurrentBatches = configuredMaxConcurrentBatches > 0
                ? configuredMaxConcurrentBatches
                : DEFAULT_MAX_CONCURRENT_BATCHES;
        this.singleTimeout = Duration.ofSeconds(singleTimeoutSeconds);
        this.batchTimeout = Duration.ofSeconds(batchTimeoutSeconds);
        this.singleRetryCount = Math.max(singleRetryCount, 0);
        this.batchRetryCount = Math.max(batchRetryCount, 0);
        log.info(
                "Initialized OllamaEmbeddingService model={} baseUrl={} maxInMemory={} batchSize={} maxConcurrentBatches={} singleTimeout={}s batchTimeout={}s singleRetry={} batchRetry={}",
                model, baseUrl, maxInMemorySize, this.batchSize, this.maxConcurrentBatches,
                singleTimeoutSeconds, batchTimeoutSeconds, this.singleRetryCount, this.batchRetryCount
        );
    }

    @Override
    public List<Float> embed(String text) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Embedding text cannot be null or blank"
            );
        }

        try {

            EmbeddingRequest request = new EmbeddingRequest(
                    model,
                    text
            );

            OllamaEmbeddingResponse response = webClient.post()
                    .uri("/api/embed")

                    .contentType(MediaType.APPLICATION_JSON)

                    .bodyValue(request)

                    .retrieve()

                    .bodyToMono(OllamaEmbeddingResponse.class)

                    .timeout(singleTimeout)

                    .retry(singleRetryCount)

                    .block();

            if (response == null) {
                throw new EmbeddingException(
                        "Received null response from Ollama"
                );
            }

            List<Float> embedding = response.firstEmbedding();

            if (embedding == null || embedding.isEmpty()) {
                throw new EmbeddingException(
                        "Received empty embedding from Ollama"
                );
            }

            if (embedding.size() != dimension) {
                throw new EmbeddingException(
                        "Invalid embedding dimension. Expected="
                                + dimension
                                + ", actual="
                                + embedding.size()
                );
            }

            List<Float> normalized =
                    EmbeddingUtils.normalize(embedding);

            log.debug(
                    "Generated embedding successfully. textLength={}, dimension={}",
                    text.length(),
                    normalized.size()
            );

            return normalized;

        } catch (Exception e) {
            Throwable root = Exceptions.unwrap(e);
            if (root instanceof TimeoutException) {
                throw new EmbeddingException(
                        "Embedding request timed out after " + singleTimeout.toSeconds() + "s",
                        e
                );
            }

            log.error(
                    "Failed to generate embedding. model={}, error={}",
                    model,
                    e.getMessage(),
                    e
            );

            throw new EmbeddingException(
                    "Failed to generate embedding",
                    e
            );
        }
    }



    /**
     * Sinh embedding vector cho nhiều đoạn văn bản cùng lúc.
     * <p>
     * Chiến lược:
     * 1. Phân chia danh sách đầu vào thành các batch nhỏ (kích thước BATCH_SIZE = 64).
     * 2. Mỗi batch gửi một HTTP POST lên Ollama /api/embed với trường "input" là List&lt;String&gt;.
     *    Ollama hỗ trợ batch input nên đây là cách hiệu quả nhất để tránh overhead HTTP.
     * 3. Các batch được gửi song song, giới hạn tối đa MAX_CONCURRENT_BATCHES = 4 tác vụ đồng thời
     *    thông qua Semaphore để tránh làm quá tải máy chủ Ollama cục bộ.
     * 4. Kết quả các batch được ghép lại theo đúng thứ tự ban đầu.
     * </p>
     *
     * @param texts Danh sách chuỗi văn bản cần sinh vector
     * @return Danh sách các vector embedding theo đúng thứ tự ban đầu
     */
    @Override
    public List<List<Float>> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            log.warn("Empty text list provided for batch embedding");
            return new ArrayList<>();
        }

        log.info("Starting batch embedding for {} texts (batchSize={}, maxConcurrent={})",
                texts.size(), batchSize, maxConcurrentBatches);

        // Phân chia thành các batch nhỏ
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < texts.size(); i += batchSize) {
            batches.add(texts.subList(i, Math.min(i + batchSize, texts.size())));
        }
        log.info("Divided {} texts into {} batches", texts.size(), batches.size());

        // Semaphore để giới hạn số batch đồng thời
        Semaphore semaphore = new Semaphore(maxConcurrentBatches);

        // Tạo CompletableFuture cho từng batch (chạy song song)
        List<CompletableFuture<List<List<Float>>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(() -> {
                    try {
                        semaphore.acquire();
                        try {
                            return embedBatchChunk(batch);
                        } finally {
                            semaphore.release();
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Batch embedding interrupted", ie);
                        return Collections.<List<Float>>nCopies(batch.size(), new ArrayList<>());
                    }
                }))
                .toList();

        // Thu thập kết quả theo thứ tự, ghép về một danh sách phẳng
        List<List<Float>> result = new ArrayList<>(texts.size());
        int batchIndex = 0;
        for (CompletableFuture<List<List<Float>>> future : futures) {
            try {
                result.addAll(future.join());
            } catch (Exception e) {
                log.error("Failed to retrieve batch embedding result for batch {}", batchIndex, e);
                int batchSize = batches.get(batchIndex).size();
                for (int i = 0; i < batchSize; i++) {
                    result.add(new ArrayList<>());
                }
            }
            batchIndex++;
        }

        log.info("Completed batch embedding: {}/{} vectors generated",
                result.stream().filter(v -> !v.isEmpty()).count(), texts.size());
        return result;
    }

    /**
     * Gửi một batch chuỗi văn bản lên Ollama /api/embed native batch endpoint.
     * Ollama hỗ trợ trường "input" là List&lt;String&gt; thay vì một String đơn lẻ.
     * Trả về danh sách vector đã được normalize theo thứ tự tương ứng.
     *
     * @param batchTexts Danh sách chuỗi văn bản trong một batch
     * @return Danh sách vector embedding tương ứng
     */
    private List<List<Float>> embedBatchChunk(List<String> batchTexts) {
        try {
            long startNanos = System.nanoTime();
            EmbeddingRequest request = new EmbeddingRequest(model, batchTexts);

            OllamaEmbeddingResponse response = webClient.post()
                    .uri("/api/embed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OllamaEmbeddingResponse.class)
                    .timeout(batchTimeout)
                    .retry(batchRetryCount)
                    .block();

            if (response == null || response.allEmbeddings() == null) {
                log.warn("Null batch response from Ollama for batch size={}", batchTexts.size());
                return Collections.nCopies(batchTexts.size(), new ArrayList<>());
            }

            List<List<Float>> embeddings = response.allEmbeddings();
            List<List<Float>> normalized = new ArrayList<>(embeddings.size());
            for (List<Float> embedding : embeddings) {
                if (embedding == null || embedding.isEmpty()) {
                    normalized.add(new ArrayList<>());
                } else {
                    normalized.add(EmbeddingUtils.normalize(embedding));
                }
            }

            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            log.info("Batch embedded successfully. batchSize={} elapsedMs={}", batchTexts.size(), elapsedMs);
            return normalized;

        } catch (Exception e) {
            Throwable root = Exceptions.unwrap(e);
            if (root instanceof TimeoutException) {
                log.error("Batch embedding timed out after {}s. batchSize={} model={}",
                        batchTimeout.toSeconds(), batchTexts.size(), model, e);
            } else if (root instanceof WebClientResponseException responseException) {
                log.error("Batch embedding HTTP error. status={} body={} batchSize={} model={}",
                        responseException.getStatusCode(),
                        responseException.getResponseBodyAsString(),
                        batchTexts.size(), model, e);
            } else if (root instanceof WebClientRequestException) {
                log.error("Batch embedding network error. batchSize={} model={} message={}",
                        batchTexts.size(), model, root.getMessage(), e);
            } else {
                log.error("Failed to embed batch chunk of size {}: {}", batchTexts.size(), e.getMessage(), e);
            }

            log.warn("Falling back to sequential embedding for batchSize={}", batchTexts.size());
            List<List<Float>> fallback = new ArrayList<>(batchTexts.size());
            for (String text : batchTexts) {
                try {
                    fallback.add(embed(text));
                } catch (Exception ex) {
                    log.error("Sequential fallback also failed for text: {}",
                            text.substring(0, Math.min(50, text.length())), ex);
                    fallback.add(new ArrayList<>());
                }
            }
            return fallback;
        }
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    // Response DTO
    private static class OllamaEmbeddingResponse {
        private List<List<Float>> embeddings;

        public List<List<Float>> getEmbeddings() {
            return embeddings;
        }

        public void setEmbeddings(List<List<Float>> embeddings) {
            this.embeddings = embeddings;
        }

        public List<Float> firstEmbedding() {
            if (embeddings == null || embeddings.isEmpty()) {
                return null;
            }
            return embeddings.getFirst();
        }

        /**
         * Trả về toàn bộ danh sách vector embedding từ phản hồi Ollama batch.
         */
        public List<List<Float>> allEmbeddings() {
            return embeddings;
        }
    }
}
