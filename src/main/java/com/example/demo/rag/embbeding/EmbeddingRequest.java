package com.example.demo.rag.embbeding;

public record EmbeddingRequest(
        String model,
        Object input
) {
}
