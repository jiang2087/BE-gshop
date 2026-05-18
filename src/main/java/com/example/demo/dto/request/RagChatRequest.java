package com.example.demo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RagChatRequest(
        String conversationId,
        @NotBlank String query,
        @Min(1) Integer limit
) {
    public RagChatRequest {
        limit = (limit == null) ? 8 : limit;
    }
}
