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
                .map(result -> {
                    String priceInfo = formatPriceInfo(result.getMinPrice(), result.getMaxPrice());
                    return String.format(
                            "- productId=%s | documentId=%s | name=%s | brand=%s | type=%s%s | score=%.4f%n  %s",
                            result.getProductId(),
                            result.getDocumentId(),
                            safe(result.getName()),
                            safe(result.getBrand()),
                            safe(result.getType()),
                            priceInfo,
                            result.getScore() == null ? 0f : result.getScore(),
                            safe(result.getText())
                    );
                })
                .collect(Collectors.joining("\n"));
    }

    private String formatPriceInfo(Double minPrice, Double maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return "";
        }
        if (minPrice != null && maxPrice != null) {
            if (minPrice.equals(maxPrice)) {
                return String.format(" | price=%.0f VND", minPrice);
            } else {
                return String.format(" | priceRange=%.0f-%.0f VND", minPrice, maxPrice);
            }
        }
        if (minPrice != null) {
            return String.format(" | minPrice=%.0f VND", minPrice);
        }
        return String.format(" | maxPrice=%.0f VND", maxPrice);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}