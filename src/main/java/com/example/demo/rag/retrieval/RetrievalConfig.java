package com.example.demo.rag.retrieval;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for dense retrieval
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "retrieval")
public class RetrievalConfig {
    
    /**
     * Default number of results to return
     */
    private int defaultLimit = 10;
    
    /**
     * Default minimum score threshold (0-1)
     */
    private float defaultScoreThreshold = 0.0f;
    
    /**
     * Maximum number of results allowed
     */
    private int maxLimit = 100;
    
    /**
     * Enable query expansion
     */
    private boolean enableQueryExpansion = false;
    
    /**
     * Enable result reranking
     */
    private boolean enableReranking = false;
    
    /**
     * Cache search results
     */
    private boolean enableCache = false;
    
    /**
     * Cache TTL in seconds
     */
    private int cacheTtlSeconds = 300;
}
