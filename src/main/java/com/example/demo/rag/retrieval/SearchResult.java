package com.example.demo.rag.retrieval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a search result from dense retrieval
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    
    /**
     * Unique point ID from Qdrant
     */
    private Long pointId;
    
    /**
     * Product ID
     */
    private Long productId;
    
    /**
     * Chunk index
     */
    private Integer chunkIndex;
    
    /**
     * Similarity score (0-1, higher is better)
     */
    private Float score;
    
    /**
     * Text content of the chunk
     */
    private String text;
    
    /**
     * Product name
     */
    private String name;
    
    /**
     * Product brand
     */
    private String brand;
    
    /**
     * Product type
     */
    private String type;
    
    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;
    
    /**
     * Get document ID in format "productId-chunkIndex"
     */
    public String getDocumentId() {
        return productId + "-" + chunkIndex;
    }
}
