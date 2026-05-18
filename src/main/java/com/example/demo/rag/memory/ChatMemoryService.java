package com.example.demo.rag.memory;

import java.util.List;

public interface ChatMemoryService {
    List<ChatTurn> getConversation(String conversationId);
    void appendUserMessage(String conversationId, String content);
    void appendAssistantMessage(String conversationId, String content);
    void clearConversation(String conversationId);
    void clearAllConversations();
}
