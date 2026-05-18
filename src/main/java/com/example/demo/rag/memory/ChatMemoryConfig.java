package com.example.demo.rag.memory;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "chat.memory")
public class ChatMemoryConfig {
    private String keyPrefix = "chat:memory:";
    private int maxTurns = 12;
    private Duration ttl = Duration.ofMinutes(60);
}
