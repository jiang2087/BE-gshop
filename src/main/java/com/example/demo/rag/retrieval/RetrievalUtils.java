package com.example.demo.rag.retrieval;

import java.util.*;
import java.util.stream.Collectors;
/**
 * Utility class for retrieval operations
 */
public class RetrievalUtils {

    /**
     * Group search results by product ID
     * @param results List of search results
     * @return Map of productId to list of results
     */

    public static Map<Long, List<SearchResult>> groupByProduct(List<SearchResult> results) {
        return results.stream()
                .filter(r -> r.getProductId() != null)
                .collect(Collectors.groupingBy(SearchResult::getProductId));
    }

    /**
     * Get top result for each product (highest score)
     * @param results List of search results
     * @return List of top results per product
     */
    public static List<SearchResult> getTopResultPerProduct(List<SearchResult> results) {
        return results.stream()
                .filter(r -> r.getProductId() != null && r.getScore() != null)
                .collect(Collectors.groupingBy(
                        SearchResult::getProductId,
                        Collectors.maxBy(Comparator.comparing(SearchResult::getScore))
                ))
                .values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(SearchResult::getScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Merge text from multiple chunks of the same product
     * @param results List of search results for the same product
     * @return Combined text
     */
    public static String mergeChunkTexts(List<SearchResult> results) {
        return results.stream()
                .sorted(Comparator.comparing(
                        SearchResult::getChunkIndex,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .map(SearchResult::getText)
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * Filter results by minimum score
     * @param results List of search results
     * @param minScore Minimum score threshold
     * @return Filtered results
     */
    public static List<SearchResult> filterByScore(List<SearchResult> results, float minScore) {
        return results.stream()
                .filter(r -> r.getScore() != null && r.getScore() >= minScore)
                .collect(Collectors.toList());
    }

    /**
     * Get unique product IDs from results
     * @param results List of search results
     * @return Set of unique product IDs
     */
    public static Set<Long> getUniqueProductIds(List<SearchResult> results) {
        return results.stream()
                .map(SearchResult::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Calculate average score for results
     * @param results List of search results
     * @return Average score
     */
    public static double calculateAverageScore(List<SearchResult> results) {
        return results.stream()
                .map(SearchResult::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(Float::doubleValue)
                .average()
                .orElse(0.0);
    }

    /**
     * Get results for a specific product
     * @param results List of search results
     * @param productId Product ID to filter by
     * @return Filtered results
     */
    public static List<SearchResult> getResultsForProduct(List<SearchResult> results, Long productId) {
        return results.stream()
                .filter(r -> Objects.equals(r.getProductId(), productId))
                .sorted(Comparator.comparing(
                        SearchResult::getChunkIndex,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .collect(Collectors.toList());
    }

    /**
     * Deduplicate results by keeping highest scoring chunk per product
     * @param results List of search results
     * @return Deduplicated results
     */
    public static List<SearchResult> deduplicateByProduct(List<SearchResult> results) {
        return getTopResultPerProduct(results);
    }

    /**
     * Format search result as readable text
     * @param result Search result
     * @return Formatted text
     */
    public static String formatResult(SearchResult result) {
        return String.format(
                "[Score: %.3f] %s - %s (Brand: %s, Type: %s)\n%s",
                result.getScore(),
                result.getName(),
                result.getDocumentId(),
                result.getBrand(),
                result.getType(),
                result.getText()
        );
    }

    /**
     * Create a summary of search results
     * @param results List of search results
     * @return Summary text
     */
    public static String createSummary(List<SearchResult> results) {
        if (results.isEmpty()) {
            return "No results found.";
        }

        int totalResults = results.size();
        int uniqueProducts = getUniqueProductIds(results).size();
        double avgScore = calculateAverageScore(results);
        float maxScore = results.stream()
                .map(SearchResult::getScore)
                .filter(Objects::nonNull)
                .max(Float::compareTo)
                .orElse(0f);
        float minScore = results.stream()
                .map(SearchResult::getScore)
                .filter(Objects::nonNull)
                .min(Float::compareTo)
                .orElse(0f);

        return String.format(
                "Found %d results from %d unique products. " +
                "Scores: avg=%.3f, max=%.3f, min=%.3f",
                totalResults, uniqueProducts, avgScore, maxScore, minScore
        );
    }

    /**
     * Normalize scores to 0-1 range
     * @param results List of search results
     * @return Results with normalized scores
     */
    public static List<SearchResult> normalizeScores(List<SearchResult> results) {
        if (results.isEmpty()) {
            return results;
        }

        float maxScore = results.stream()
                .map(SearchResult::getScore)
                .filter(Objects::nonNull)
                .max(Float::compareTo)
                .orElse(1f);

        float minScore = results.stream()
                .map(SearchResult::getScore)
                .filter(Objects::nonNull)
                .min(Float::compareTo)
                .orElse(0f);

        float range = maxScore - minScore;
        if (range == 0) {
            return results;
        }

        return results.stream()
                .peek(r -> {
                    if (r.getScore() == null) {
                        return;
                    }
                    float normalizedScore = (r.getScore() - minScore) / range;
                    r.setScore(normalizedScore);
                })
                .collect(Collectors.toList());
    }
}
