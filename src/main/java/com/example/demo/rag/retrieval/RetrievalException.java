package com.example.demo.rag.retrieval;

/**
 * Exception thrown when retrieval operations fail
 */
public class RetrievalException extends RuntimeException {
    
    public RetrievalException(String message) {
        super(message);
    }
    
    public RetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
