package com.example.demo.rag.chunking;

import java.util.List;

/**
 * Interface for text chunking strategies
 */
public interface TextChunker {
    
    /**
     * Split text into chunks
     * @param text Input text to chunk
     * @return List of text chunks
     */
    List<String> chunk(String text);
    
    /**
     * Split text into chunks with metadata
     * @param text Input text to chunk
     * @return List of chunks with metadata
     */
    List<Chunk> chunkWithMetadata(String text);
}
