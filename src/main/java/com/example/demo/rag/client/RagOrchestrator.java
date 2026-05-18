package com.example.demo.rag.client;

import com.example.demo.dto.message_ai.ChatCompletionResponse;
import com.example.demo.dto.message_ai.Choice;
import com.example.demo.dto.response.RagChatResponse;
import com.example.demo.dto.response.RagProductResponse;
import com.example.demo.rag.prompt.PromptBuilder;
import com.example.demo.rag.prompt.RetrievalContext;
import com.example.demo.rag.retrieval.DenseRetrievalService;
import com.example.demo.rag.retrieval.SearchRequest;
import com.example.demo.rag.retrieval.SearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RagOrchestrator {
    private final DenseRetrievalService denseRetrievalService;
    private final ChatApiClient chatApiClient;
    private final ProductQueryDetector productQueryDetector;
    private final ChatClientConfig config;

    public RagChatResponse executeRagFlow(String userQuery, int retrievalLimit) {
        validateInput(userQuery);

        List<SearchResult> retrieved = retrieveContext(userQuery, retrievalLimit);
        String systemPrompt = buildSystemPrompt(userQuery, retrieved);
        String answer = generateAnswer(systemPrompt, userQuery);
        List<RagProductResponse> products = extractProducts(userQuery, retrieved);

        return new RagChatResponse(null, answer, products);
    }

    private void validateInput(String userQuery) {
        if (denseRetrievalService == null) {
            throw new IllegalStateException("DenseRetrievalService is required for RAG flow");
        }
        if (userQuery == null || userQuery.isBlank()) {
            throw new IllegalArgumentException("userQuery must not be blank");
        }
    }

    private List<SearchResult> retrieveContext(String userQuery, int retrievalLimit) {
        return denseRetrievalService.search(
                SearchRequest.builder()
                        .query(userQuery)
                        .limit(retrievalLimit)
                        .build()
        ).getResults();
    }

    private String buildSystemPrompt(String userQuery, List<SearchResult> retrieved) {
        RetrievalContext retrievalContext = RetrievalContext.builder()
                .query(userQuery)
                .results(retrieved)
                .build();
        return PromptBuilder.buildSystemPrompt(retrievalContext);
    }

    private String generateAnswer(String systemPrompt, String userQuery) {
        List<Map<String, Object>> ragMessages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userQuery)
        );

        ChatCompletionResponse completionResponse = chatApiClient.sendRequest(
                config.getDefaultModel(), ragMessages, null, null, false
        );
        return extractFirstAssistantMessage(completionResponse);
    }

    private List<RagProductResponse> extractProducts(String userQuery, List<SearchResult> retrieved) {
        if (!productQueryDetector.isProductRelatedQuery(userQuery)) {
            return List.of();
        }

        Set<Long> productIds = new LinkedHashSet<>();
        for (SearchResult result : retrieved) {
            if (result != null && result.getProductId() != null) {
                productIds.add(result.getProductId());
            }
        }

        return productIds.stream()
                .map(RagProductResponse::new)
                .toList();
    }

    private String extractFirstAssistantMessage(ChatCompletionResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return "";
        }
        Choice firstChoice = response.getChoices().getFirst();
        if (firstChoice == null || firstChoice.getMessage() == null || firstChoice.getMessage().getContent() == null) {
            return "";
        }
        return firstChoice.getMessage().getContent();
    }
}
