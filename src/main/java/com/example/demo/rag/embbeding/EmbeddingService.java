package com.example.demo.rag.embbeding;

import java.util.List;

public interface EmbeddingService {
    
    /**
     * Generate embedding vector for a single text
     * @param text Input text to embed
     * @return Embedding vector as list of floats
     */
    List<Float> embed(String text);
    
    /**
     * Generate embedding vectors for multiple texts
     * @param texts List of input texts to embed
     * @return List of embedding vectors
     */
    List<List<Float>> embedBatch(List<String> texts);
    
    /**
     * Get the dimension of the embedding vectors
     * @return Dimension size
     */
    int getDimension();
}
