package com.example.demo.dto.response;

import java.util.List;

public record RagChatResponse(
        String conversationId,
        String answer,
        List<Long> products
) {
}
