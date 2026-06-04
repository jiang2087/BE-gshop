package com.example.demo.rag.client;

import com.example.demo.dto.message_ai.ChatCompletionResponse;
import com.example.demo.dto.message_ai.Choice;
import com.example.demo.dto.message_ai.Message;
import com.example.demo.dto.response.RagChatResponse;
import com.example.demo.rag.memory.ChatMemoryConfig;
import com.example.demo.rag.memory.ChatTurn;
import com.example.demo.rag.prompt.PromptBuilder;
import com.example.demo.rag.prompt.RetrievalContext;
import com.example.demo.rag.retrieval.DenseRetrievalService;
import com.example.demo.rag.retrieval.SearchRequest;
import com.example.demo.rag.retrieval.SearchResult;
import com.example.demo.rag.tools.ToolExecutor;
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
    private final ToolCallOrchestrator toolCallOrchestrator;
    private final ChatApiClient chatApiClient;
    private final ToolExecutor toolExecutor;
    private final ProductQueryDetector productQueryDetector;
    private final ChatClientConfig config;
    private final ChatMemoryConfig memoryConfig;

    private volatile List<Map<String, Object>> cachedToolSpecs;

    private record AssistantReply(String content) {}

    public RagChatResponse executeRagFlow(String userQuery, int retrievalLimit) {
        return executeRagFlow(userQuery, retrievalLimit, List.of());
    }

    public RagChatResponse executeRagFlow(String userQuery, int retrievalLimit, List<ChatTurn> conversationHistory) {
        validateInput(userQuery);

        List<SearchResult> retrieved = retrieveContext(userQuery, retrievalLimit);
        String systemPrompt = buildSystemPrompt(userQuery, retrieved);
        AssistantReply reply = generateAnswer(systemPrompt, userQuery, conversationHistory);
        List<Long> products = extractProducts(userQuery, retrieved);

        return new RagChatResponse(null, reply.content(), products, null);
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

    private AssistantReply generateAnswer(String systemPrompt, String userQuery, List<ChatTurn> conversationHistory) {
        List<Map<String, Object>> ragMessages = new java.util.ArrayList<>();
        
        // Add system prompt
        ragMessages.add(Map.of("role", "system", "content", systemPrompt));
        
        // Add conversation history if exists
        List<ChatTurn> promptHistory = trimHistoryForPrompt(conversationHistory);
        if (!promptHistory.isEmpty()) {
            for (ChatTurn turn : promptHistory) {
                Map<String, Object> msg = new java.util.HashMap<>();
                msg.put("role", turn.role());
                msg.put("content", turn.content());
                ragMessages.add(msg);
            }
        }
        
        // Add current user query
        ragMessages.add(Map.of("role", "user", "content", userQuery));

        // Use ToolCallOrchestrator to enable tool calling
        List<Map<String, Object>> tools = getToolSpecs();
        ChatCompletionResponse completionResponse;
        if (!tools.isEmpty() && toolCallOrchestrator.shouldAttemptToolFlow(userQuery, tools)) {
            completionResponse = toolCallOrchestrator.executeWithTools(
                    config.getDefaultModel(), ragMessages, tools, null
            );
        } else {
            completionResponse = chatApiClient.sendRequest(
                    config.getDefaultModel(), ragMessages, null, null, false
            );
        }
        return extractFirstChoiceMessage(completionResponse);
    }

    private List<ChatTurn> trimHistoryForPrompt(List<ChatTurn> conversationHistory) {
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            return List.of();
        }

        int maxTurns = Math.max(1, memoryConfig.getMaxPromptTurns());
        int maxChars = Math.max(500, memoryConfig.getMaxPromptChars());
        List<ChatTurn> selected = new java.util.ArrayList<>();
        int usedChars = 0;

        for (int i = conversationHistory.size() - 1; i >= 0 && selected.size() < maxTurns; i--) {
            ChatTurn turn = conversationHistory.get(i);
            if (turn == null || turn.content() == null || turn.content().isBlank()) {
                continue;
            }
            ChatTurn promptTurn = truncateTurn(turn, Math.max(1, maxChars - usedChars));
            int contentChars = promptTurn.content().length();
            if (!selected.isEmpty() && usedChars + contentChars > maxChars) {
                break;
            }
            selected.add(promptTurn);
            usedChars += contentChars;
        }

        java.util.Collections.reverse(selected);
        return selected;
    }

    private ChatTurn truncateTurn(ChatTurn turn, int maxChars) {
        if (turn.content().length() <= maxChars) {
            return turn;
        }
        String content = turn.content().substring(0, Math.max(0, maxChars - 3)) + "...";
        return new ChatTurn(turn.role(), content, null, turn.timestamp());
    }

    private List<Map<String, Object>> getToolSpecs() {
        List<Map<String, Object>> local = cachedToolSpecs;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (cachedToolSpecs == null) {
                List<Map<String, Object>> specs = toolExecutor.buildToolSpecs();
                cachedToolSpecs = specs == null ? List.of() : List.copyOf(specs);
            }
            return cachedToolSpecs;
        }
    }

    private List<Long> extractProducts(String userQuery, List<SearchResult> retrieved) {
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
                .toList();
    }

    private AssistantReply extractFirstChoiceMessage(ChatCompletionResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return new AssistantReply("");
        }
        Choice firstChoice = response.getChoices().getFirst();
        if (firstChoice == null || firstChoice.getMessage() == null) {
            return new AssistantReply("");
        }
        Message msg = firstChoice.getMessage();
        return new AssistantReply(msg.getContent() != null ? msg.getContent() : "");
    }
}
