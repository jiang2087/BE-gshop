package com.example.demo.rag.connection;

import com.example.demo.rag.embbeding.EmbeddingService;
import io.qdrant.client.QdrantClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RagConnectionService {

    private final EmbeddingService embeddingService;
    private final QdrantClient qdrantClient;

    @Value("${qdrant.collection-name:product_variants}")
    private String collectionName;

    public ConnectionStatus testOllama() {
        try {
            int dimension = embeddingService.embed("ping").size();
            return new ConnectionStatus(true, "Ollama connected", dimension);
        } catch (Exception ex) {
            return new ConnectionStatus(false, "Ollama failed: " + ex.getMessage(), null);
        }
    }

    public ConnectionStatus testQdrant() {
        try {
            boolean collectionExists = qdrantClient.collectionExistsAsync(collectionName).get();
            String message = collectionExists
                    ? "Qdrant connected, collection exists"
                    : "Qdrant connected, collection missing";
            return new ConnectionStatus(true, message, null);
        } catch (Exception ex) {
            return new ConnectionStatus(false, "Qdrant failed: " + ex.getMessage(), null);
        }
    }

    public ConnectionTestResponse testAll() {
        ConnectionStatus ollama = testOllama();
        ConnectionStatus qdrant = testQdrant();
        boolean healthy = ollama.connected() && qdrant.connected();
        return new ConnectionTestResponse(healthy, ollama, qdrant);
    }

    public record ConnectionStatus(boolean connected, String message, Integer embeddingDimension) {}

    public record ConnectionTestResponse(boolean healthy, ConnectionStatus ollama, ConnectionStatus qdrant) {}
}
