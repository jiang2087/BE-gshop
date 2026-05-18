package com.example.demo.rag.ingestion;

/**
 * Exception thrown when ingestion fails
 */
public class IngestionException extends RuntimeException {
    
    public IngestionException(String message) {
        super(message);
    }
    
    public IngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}
