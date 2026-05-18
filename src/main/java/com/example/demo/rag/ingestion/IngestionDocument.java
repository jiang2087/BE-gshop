package com.example.demo.rag.ingestion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a document to be ingested into the vector database
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionDocument {
    
    /**
     * Unique identifier for the document
     * Format: "{productId}-{chunkIndex}"
     */
    private String id;
    
    /**
     * Embedding vector
     */
    private List<Float> vector;
    
    /**
     * Payload containing metadata
     */
    private IngestionPayload payload;
}
