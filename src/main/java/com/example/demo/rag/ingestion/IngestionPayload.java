package com.example.demo.rag.ingestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload containing metadata for ingested documents
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionPayload {
    
    /**
     * Product ID
     */
    private Long productId;
    
    /**
     * Chunk index within the product
     */
    private Integer chunkIndex;
    
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
     * Product type (LAPTOP, MOBILE, WATCHES, TELEVISION)
     */
    private String type;
    
    /**
     * Number of tokens in the chunk
     */
    private Integer tokenCount;
    
    /**
     * Number of sentences in the chunk
     */
    private Integer sentenceCount;
}