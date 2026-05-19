package com.example.demo.rag.chunking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SentenceAwareChunkerTest {

    private SentenceAwareChunker chunker;
    private ChunkingConfig config;

    @BeforeEach
    void setUp() {
        config = new ChunkingConfig();
        config.setChunkSize(512);
        config.setOverlap(50);
        config.setMinChunkSize(128);
        chunker = new SentenceAwareChunker(config);
    }

    @Test
    void testShortText_ShouldCreateOneChunk() {
        // Text < 512 chars
        String shortText = "This is a short product description. " +
                "It has some features. " +
                "The price is good. " +
                "Buy it now.";
        
        List<Chunk> chunks = chunker.chunkWithMetadata(shortText);
        
        System.out.println("=== SHORT TEXT TEST ===");
        System.out.println("Text length: " + shortText.length());
        System.out.println("Number of chunks: " + chunks.size());
        for (Chunk chunk : chunks) {
            System.out.println("  Chunk " + chunk.getChunkIndex() + ": " + chunk.getContent().length() + " chars");
        }
        
        assertEquals(1, chunks.size(), "Short text should create 1 chunk");
        assertEquals(0, chunks.get(0).getChunkIndex(), "First chunk should have index 0");
    }

    @Test
    void testLongText_ShouldCreateMultipleChunks() {
        // Text > 512 chars - simulate realistic product description
        StringBuilder sb = new StringBuilder();
        sb.append("Samsung Galaxy S24 Ultra mobile phone by Samsung.\n");
        sb.append("Galaxy S24 Ultra model.\n");
        sb.append("6.8 inch screen.\n");
        sb.append("3088x1440 resolution.\n");
        sb.append("200MP wide, 50MP periscope telephoto camera.\n");
        sb.append("5000mAh battery.\n");
        sb.append("162.3 x 79 x 8.6 mm dimension.\n\n");
        sb.append("Best for:\n");
        sb.append("- high-quality photography\n");
        sb.append("- high-quality video recording\n");
        sb.append("- content creation\n");
        sb.append("- heavy daily usage\n");
        sb.append("- long battery life\n");
        sb.append("- travel\n");
        sb.append("- high-quality streaming\n");
        sb.append("- gaming\n");
        sb.append("- entertainment\n");
        sb.append("- video watching\n\n");
        sb.append("Category:\n");
        sb.append("premium smartphone\n");
        sb.append("flagship phone\n");
        
        // Add more text to exceed 512 chars
        sb.append("\nAdditional features include advanced AI processing, ");
        sb.append("enhanced night mode photography, 8K video recording capability, ");
        sb.append("S Pen support for productivity, wireless charging, ");
        sb.append("water resistance IP68 rating, and premium build quality. ");
        sb.append("The device features the latest Snapdragon processor ");
        sb.append("with exceptional performance for multitasking and gaming. ");
        sb.append("Display supports 120Hz refresh rate for smooth scrolling. ");
        sb.append("Camera system includes advanced zoom capabilities up to 100x. ");
        sb.append("Battery supports fast charging and wireless power share.");
        
        String longText = sb.toString();
        
        List<Chunk> chunks = chunker.chunkWithMetadata(longText);
        
        System.out.println("\n=== LONG TEXT TEST ===");
        System.out.println("Text length: " + longText.length());
        System.out.println("Number of chunks: " + chunks.size());
        for (Chunk chunk : chunks) {
            System.out.println("  Chunk " + chunk.getChunkIndex() + ": " + 
                             chunk.getContent().length() + " chars, " +
                             chunk.getSentenceCount() + " sentences");
            System.out.println("    Preview: " + chunk.getContent().substring(0, Math.min(80, chunk.getContent().length())) + "...");
        }
        
        assertTrue(longText.length() > 512, "Text should be longer than chunk size");
        
        if (chunks.size() == 1) {
            fail("CHUNKING LOGIC ERROR: Long text (" + longText.length() + 
                 " chars) created only 1 chunk. Expected multiple chunks.");
        }
        
        // Verify chunk indices are sequential
        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).getChunkIndex(), 
                        "Chunk at position " + i + " should have index " + i);
        }
    }

    @Test
    void testOverlapBehavior() {
        // Create text with clear sentence boundaries
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 30; i++) {
            sb.append("This is sentence number ").append(i).append(". ");
        }
        String text = sb.toString();
        
        List<Chunk> chunks = chunker.chunkWithMetadata(text);
        
        System.out.println("\n=== OVERLAP TEST ===");
        System.out.println("Text length: " + text.length());
        System.out.println("Config overlap: " + config.getOverlap());
        System.out.println("Number of chunks: " + chunks.size());
        for (Chunk chunk : chunks) {
            System.out.println("  Chunk " + chunk.getChunkIndex() + ": " + 
                             chunk.getSentenceCount() + " sentences");
        }
        
        if (chunks.size() > 1) {
            System.out.println("\nWARNING: Overlap = " + config.getOverlap() + " sentences");
            System.out.println("This is likely TOO HIGH if chunks have < 50 sentences!");
        }
    }
}
