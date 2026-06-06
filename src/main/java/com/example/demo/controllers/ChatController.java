package com.example.demo.controllers;

import com.example.demo.dto.request.RagChatRequest;
import com.example.demo.dto.response.ConversationMemoryResponse;
import com.example.demo.dto.response.ConversationResponse;
import com.example.demo.dto.response.RagChatResponse;
import com.example.demo.rag.ChatClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Validated
public class ChatController {

    private final ChatClient chatClient;

    @GetMapping("/rag")
    public ResponseEntity<RagChatResponse> ragChat(
            @RequestParam("q") @NotBlank String query,
            @RequestParam(value = "limit", defaultValue = "8") @Min(1) int limit
    ) {
        return ResponseEntity.ok(chatClient.callChatWithRag(query, limit));
    }

    @PostMapping("/rag")
    public ResponseEntity<RagChatResponse> ragChatWithMemory(@Valid @RequestBody RagChatRequest request) {
        return ResponseEntity.ok(
                chatClient.callChatWithRag(request.conversationId(), request.query(), request.limit())
        );
    }

    @GetMapping(value = "/rag/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> ragChatStream(
            @RequestParam("q") @NotBlank String query,
            @RequestParam(value = "limit", defaultValue = "8") @Min(1) int limit
    ) {
        return chatClient.callChatWithRagStream(query, limit);
    }

    @PostMapping(value = "/rag/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> ragChatStreamWithMemory(@Valid @RequestBody RagChatRequest request) {
        return chatClient.callChatWithRagStream(request.conversationId(), request.query(), request.limit());
    }

    @PostMapping("/rag/conversations")
    public ResponseEntity<ConversationResponse> createConversation() {
        return ResponseEntity.ok(new ConversationResponse(chatClient.createConversationId()));
    }

    @GetMapping("/rag/{conversationId}")
    public ResponseEntity<ConversationMemoryResponse> getConversation(
            @PathVariable("conversationId") @NotBlank String conversationId
    ) {
        return ResponseEntity.ok(
                new ConversationMemoryResponse(conversationId, chatClient.getConversation(conversationId))
        );
    }

    @DeleteMapping("/rag/{conversationId}")
    public ResponseEntity<Void> clearConversation(@PathVariable("conversationId") String conversationId) {
        chatClient.clearConversation(conversationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/rag/conversations")
    public ResponseEntity<Void> clearAllConversations() {
        chatClient.clearAllConversations();
        return ResponseEntity.noContent().build();
    }
}
