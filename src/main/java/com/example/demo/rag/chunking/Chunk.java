package com.example.demo.rag.chunking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a text chunk with metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chunk {
    
    /**
     * The actual text content of the chunk
     */
    private String content;
    
    /**
     * Starting position in the original text
     */
    private int startIndex;
    
    /**
     * Ending position in the original text
     */
    private int endIndex;
    
    /**
     * Chunk index in the sequence
     */
    private int chunkIndex;
    
    /**
     * Number of sentences in this chunk
     */
    private int sentenceCount;
    
    /**
     * Number of tokens/words in this chunk
     */
    private int tokenCount;
    
    /**
     * Overlap with previous chunk (in characters)
     */
    private int overlapSize;
    
    public Chunk(String content, int startIndex, int endIndex, int chunkIndex) {
        this.content = content;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.chunkIndex = chunkIndex;
        this.tokenCount = content.split("\\s+").length;
    }
}
