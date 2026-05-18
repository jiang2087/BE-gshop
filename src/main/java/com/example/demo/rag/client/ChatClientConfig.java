package com.example.demo.rag.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "chat.client")
@Data
public class ChatClientConfig {
    private String baseUrl = "http://localhost:20128";
    private String defaultModel = "kc/openai/gpt-4.1";
    private int maxRetryAttempts = 3;
    private Duration retryDelay = Duration.ofSeconds(2);
    private Duration requestTimeout = Duration.ofSeconds(30);
}
