package com.example.demo.rag;

import com.example.demo.dto.response.RagChatResponse;
import com.example.demo.rag.client.RagOrchestrator;
import com.example.demo.rag.memory.ChatMemoryService;
import com.example.demo.rag.memory.ChatTurn;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatClient {

    private final RagOrchestrator ragOrchestrator;
    private final ChatMemoryService chatMemoryService;



    public RagChatResponse callChatWithRag(String userQuery, int retrievalLimit) {
        return ragOrchestrator.executeRagFlow(userQuery, retrievalLimit);
    }

    public RagChatResponse callChatWithRag(String conversationId, String userQuery, int retrievalLimit) {
        if (userQuery == null || userQuery.isBlank()) {
            throw new IllegalArgumentException("userQuery must not be blank");
        }
        String effectiveConversationId = (conversationId == null || conversationId.isBlank())
                ? createConversationId()
                : conversationId;

        List<ChatTurn> history = chatMemoryService.getConversation(effectiveConversationId);
        String contextualQuery = buildContextualQuery(history, userQuery);

        RagChatResponse response = ragOrchestrator.executeRagFlow(contextualQuery, retrievalLimit);
        chatMemoryService.appendUserMessage(effectiveConversationId, userQuery);
        chatMemoryService.appendAssistantMessage(effectiveConversationId, response.answer());

        return new RagChatResponse(effectiveConversationId, response.answer(), response.products());
    }

    public void clearConversation(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId must not be blank");
        }
        chatMemoryService.clearConversation(conversationId);
    }

    public void clearAllConversations() {
        chatMemoryService.clearAllConversations();
    }

    public String createConversationId() {
        return UUID.randomUUID().toString();
    }

    public List<ChatTurn> getConversation(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId must not be blank");
        }
        return chatMemoryService.getConversation(conversationId);
    }

    private String buildContextualQuery(List<ChatTurn> history, String userQuery) {
        if (history == null || history.isEmpty()) {
            return userQuery;
        }
        String historyText = history.stream()
                .map(turn -> turn.role() + ": " + turn.content())
                .collect(Collectors.joining("\n"));
        return "Conversation history:\n" + historyText + "\n\nCurrent user question:\n" + userQuery;
    }

    private void validateInput(String model, List<Map<String, Object>> messages) {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("model must not be blank");
        }
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages must not be empty");
        }
    }
}
