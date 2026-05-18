package com.example.demo.dto.response;

import com.example.demo.rag.memory.ChatTurn;

import java.util.List;

public record ConversationMemoryResponse(
        String conversationId,
        List<ChatTurn> turns
) {
}
