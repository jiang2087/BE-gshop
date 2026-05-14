package com.example.demo.rag.embbeding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OllamaEmbeddingService implements EmbeddingService {

    private final WebClient webClient;
    private final String model;
    private final int dimension;

    public OllamaEmbeddingService(
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.embedding.model:nomic-embed-text}") String model,
            @Value("${ollama.embedding.dimension:768}") int dimension
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.model = model;
        this.dimension = dimension;
        log.info("Initialized OllamaEmbeddingService with model: {} at {}", model, baseUrl);
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

                    .timeout(Duration.ofSeconds(30))

                    .retry(2)

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

    @Override
    public List<List<Float>> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            log.warn("Empty text list provided for batch embedding");
            return new ArrayList<>();
        }

        List<List<Float>> embeddings = new ArrayList<>();
        
        for (String text : texts) {
            try {
                embeddings.add(embed(text));
            } catch (Exception e) {
                log.error("Error in batch embedding for text: {}", text.substring(0, Math.min(50, text.length())), e);
                embeddings.add(new ArrayList<>());
            }
        }

        log.info("Generated {} embeddings in batch", embeddings.size());
        return embeddings;
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
    }
}
