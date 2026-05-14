package com.example.demo.dto.request;

public record SemanticSearchRequest(
        String query,
        Integer limit,
        Float scoreThreshold
) {

    public SemanticSearchRequest {
        limit = (limit == null) ? 10 : limit;
        scoreThreshold = (scoreThreshold == null) ? 0.6f : scoreThreshold;
    }
}