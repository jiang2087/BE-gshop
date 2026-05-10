package com.example.demo.controllers;

import com.example.demo.services.ai.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/test-ai")
    public ResponseEntity<?> testAI() {
        return ResponseEntity.ok(chatService.callAI());
    }
}