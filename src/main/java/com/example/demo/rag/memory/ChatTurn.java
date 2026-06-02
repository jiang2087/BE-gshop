package com.example.demo.rag.memory;

import java.io.Serializable;
import java.time.Instant;

public record ChatTurn(
        String role,
        String content,
        String reasoningContent,
        Instant timestamp
) implements Serializable {
}
