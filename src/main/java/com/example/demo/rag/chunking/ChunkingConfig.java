package com.example.demo.rag.chunking;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "chunking")
public class ChunkingConfig {

    private int chunkSize = 1000;

    private int overlap = 0;

    private int minChunkSize = 128;

    private boolean sentenceAware = true;

    @PostConstruct
    public void validate() {

        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be > 0");
        }

        if (overlap < 0) {
            throw new IllegalArgumentException("overlap must be >= 0");
        }

        if (overlap >= chunkSize) {
            throw new IllegalArgumentException("overlap must be < chunkSize");
        }

        if (minChunkSize < 0) {
            throw new IllegalArgumentException("minChunkSize must be >= 0");
        }

        if (minChunkSize > chunkSize) {
            throw new IllegalArgumentException("minChunkSize must be <= chunkSize");
        }
    }
}
