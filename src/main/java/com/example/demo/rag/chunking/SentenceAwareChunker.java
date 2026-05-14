package com.example.demo.rag.chunking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sentence-aware text chunking implementation
 * Splits text into chunks while preserving sentence boundaries
 */
@Slf4j
@Component
public class SentenceAwareChunker implements TextChunker {

    private final ChunkingConfig config;
    // Sentence boundary patterns
    private static final Pattern SENTENCE_PATTERN = Pattern.compile(
        "[^.!?\\u3002\\uff01\\uff1f]+[.!?\\u3002\\uff01\\uff1f]+[\"\')\\]\\}]*\\s*"
    );
    
    private final int chunkSize;
    private final int overlap;

    public SentenceAwareChunker(ChunkingConfig config) {
        this.config = config;
        this.chunkSize = config.getChunkSize();
        this.overlap = config.getOverlap();
        log.info("Initialized SentenceAwareChunker with chunkSize={}, overlap={}", 
                this.chunkSize, this.overlap);
    }

    @Override
    public List<String> chunk(String text) {
        List<Chunk> chunks = chunkWithMetadata(text);
        return chunks.stream()
                .map(Chunk::getContent)
                .toList();
    }
    @Override
    public List<Chunk> chunkWithMetadata(String text) {

        if (text == null || text.isBlank()) {
            log.warn("Empty text provided for chunking");
            return List.of();
        }

        List<SentenceSpan> sentences = splitIntoSentenceSpans(text);

        List<Chunk> chunks = new ArrayList<>();

        List<SentenceSpan> buffer = new ArrayList<>();

        int chunkIndex = 0;

        int currentSize = 0;

        for (SentenceSpan sentence : sentences) {

            int sentenceSize = sentence.text().length();

            boolean wouldExceedMax = currentSize + sentenceSize > chunkSize;
            boolean belowMin = currentSize < config.getMinChunkSize();

            if (wouldExceedMax && !buffer.isEmpty() && !belowMin) {

                chunks.add(buildChunk(buffer, chunkIndex++));

                buffer = applyOverlap(buffer);

                currentSize = buffer.stream()
                        .mapToInt(s -> s.text().length())
                        .sum();
            }

            buffer.add(sentence);
            currentSize += sentenceSize;
        }

        if (!buffer.isEmpty()) {
            chunks.add(buildChunk(buffer, chunkIndex));
        }

        log.info("Created {} chunks from text length={}", chunks.size(), text.length());

        return chunks;
    }

    public record SentenceSpan(
            String text,
            int start,
            int end
    ) {}

    private List<SentenceSpan> splitIntoSentenceSpans(String text) {

        List<SentenceSpan> result = new ArrayList<>();

        Matcher matcher = Pattern.compile("[^.!?]+[.!?]?").matcher(text);

        while (matcher.find()) {

            result.add(new SentenceSpan(
                    matcher.group().trim(),
                    matcher.start(),
                    matcher.end()
            ));
        }

        return result;
    }
    private Chunk buildChunk(List<SentenceSpan> sentences, int index) {

        int start = sentences.getFirst().start();
        int end = sentences.getLast().end();

        String content = sentences.stream()
                .map(SentenceSpan::text)
                .collect(Collectors.joining(" "))
                .trim();

        Chunk chunk = new Chunk(
                content,
                start,
                end,
                index
        );

        chunk.setSentenceCount(sentences.size());
        chunk.setTokenCount(estimateTokens(content));

        return chunk;
    }

    private List<SentenceSpan> applyOverlap(List<SentenceSpan> buffer) {

        if (overlap <= 0 || buffer.isEmpty()) {
            return new ArrayList<>();
        }

        int from = Math.max(0, buffer.size() - overlap);

        return new ArrayList<>(buffer.subList(from, buffer.size()));
    }
    private int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        return text.length() / 4;
    }
}
