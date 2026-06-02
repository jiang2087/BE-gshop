package com.example.demo.rag.client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "chat.client")
@Data
public class ChatClientConfig {
    private String baseUrl = "http://localhost:20128";
    @Value("${spring.ai.openai.chat.options.model}")
    private String defaultModel;
    private int maxRetryAttempts = 3;
    private Duration retryDelay = Duration.ofSeconds(2);
    private Duration requestTimeout = Duration.ofSeconds(60);
    private BigDecimal vndToUsdRate = new BigDecimal("26000");
    private List<String> thinkingModels = List.of("deepseek-r1", "deepseek-reasoner", "o1", "o3");
}
