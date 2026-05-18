package com.example.demo.rag.prompt;

import com.example.demo.rag.retrieval.SearchResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Retrieved context payload for prompt construction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalContext {

    /**
     * Original user query
     */
    private String query;

    /**
     * Retrieved search results
     */
    private List<SearchResult> results;



    /**
     * Render context text from retrieval results
     */
    public String toContextText() {
        if (results == null || results.isEmpty()) {
            return "Khong co tai lieu lien quan.";
        }

        return results.stream()
                .filter(Objects::nonNull)
                .map(result -> String.format(
                        "- productId=%s | documentId=%s | name=%s | brand=%s | type=%s | score=%.4f%n  %s",
                        result.getProductId(),
                        result.getDocumentId(),
                        safe(result.getName()),
                        safe(result.getBrand()),
                        safe(result.getType()),
                        result.getScore() == null ? 0f : result.getScore(),
                        safe(result.getText())
                ))
                .collect(Collectors.joining("\n"));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
