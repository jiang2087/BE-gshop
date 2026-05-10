package com.example.demo.services.ai;


import com.example.demo.dto.message_ai.ChatCompletionResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final WebClient webClient;

    public ChatService(WebClient webClient) {
        this.webClient = webClient;
    }

    public ChatCompletionResponse callAI() {

        Map<String, Object> body = Map.of(
                "model", "ollama/qwen3-coder:480b-cloud",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are AI"),
                        Map.of("role", "user", "content", "Hello from 3030")
                ),
                "temperature", 0.7,
                "stream", false
        );

        return webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(body)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        resp -> resp.bodyToMono(String.class)
                                .map(msg -> new RuntimeException("API error: " + msg))
                )
                .bodyToMono(ChatCompletionResponse.class)
                .block();
    }
}