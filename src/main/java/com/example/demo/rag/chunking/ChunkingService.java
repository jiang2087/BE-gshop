package com.example.demo.rag.chunking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for chunking text with various strategies
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkingService {

    private final SentenceAwareChunker sentenceAwareChunker;
    private final ChunkingConfig config;

    public List<String> chunkText(String text) {

        if (text == null || text.isBlank()) {
            log.warn("Empty text provided for chunking");
            return List.of();
        }

        return sentenceAwareChunker.chunk(text);
    }

    public List<Chunk> chunkTextWithMetadata(String text) {

        if (text == null || text.isBlank()) {
            log.warn("Empty text provided for chunking");
            return List.of();
        }

        return sentenceAwareChunker.chunkWithMetadata(text);
    }

    /**
     * Chunk multiple texts
     *
     * @param texts List of input texts
     * @return List of lists of chunks
     */
    public List<List<String>> chunkBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            log.warn("Empty text list provided for batch chunking");
            return new ArrayList<>();
        }

        return texts.stream()
                .map(this::chunkText)
                .toList();
    }

    public ChunkingStats getChunkingStats(String text) {

        List<Chunk> chunks = chunkTextWithMetadata(text);

        if (chunks.isEmpty()) {
            return new ChunkingStats(0, 0, 0, 0, 0, 0);
        }

        int totalTokens = chunks.stream()
                .mapToInt(Chunk::getTokenCount)
                .sum();

        return new ChunkingStats(
                chunks.size(),
                totalTokens,
                totalTokens / chunks.size(),
                chunks.stream().mapToInt(Chunk::getTokenCount).max().orElse(0),
                chunks.stream().mapToInt(Chunk::getTokenCount).min().orElse(0),
                text.length()
        );
    }

    /**
     * Statistics about chunking results
     */
    public record ChunkingStats(
            int totalChunks,
            int totalTokens,
            int avgTokensPerChunk,
            int maxTokens,
            int minTokens,
            int originalTextLength
    ) {
    }


}