package com.example.demo.rag.embbeding;

import java.util.List;

/**
 * Utility class for embedding vector operations
 */
public class EmbeddingUtils {

    /**
     * Calculate cosine similarity between two embedding vectors
     * @param vec1 First embedding vector
     * @param vec2 Second embedding vector
     * @return Cosine similarity score (0 to 1)
     */
    public static double cosineSimilarity(List<Float> vec1, List<Float> vec2) {
        if (vec1 == null || vec2 == null || vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("Vectors must be non-null and of equal length");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Calculate Euclidean distance between two embedding vectors
     * @param vec1 First embedding vector
     * @param vec2 Second embedding vector
     * @return Euclidean distance
     */
    public static double euclideanDistance(List<Float> vec1, List<Float> vec2) {
        if (vec1 == null || vec2 == null || vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("Vectors must be non-null and of equal length");
        }

        double sum = 0.0;
        for (int i = 0; i < vec1.size(); i++) {
            double diff = vec1.get(i) - vec2.get(i);
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    /**
     * Normalize an embedding vector to unit length
     * @param vector Input vector
     * @return Normalized vector
     */
    public static List<Float> normalize(List<Float> vector) {
        if (vector == null || vector.isEmpty()) {
            throw new IllegalArgumentException("Vector must be non-null and non-empty");
        }

        double norm = 0.0;
        for (Float value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);

        if (norm == 0.0) {
            return vector;
        }

        final double finalNorm = norm;
        return vector.stream()
                .map(v -> (float) (v / finalNorm))
                .toList();
    }

    /**
     * Calculate dot product of two vectors
     * @param vec1 First vector
     * @param vec2 Second vector
     * @return Dot product
     */
    public static double dotProduct(List<Float> vec1, List<Float> vec2) {
        if (vec1 == null || vec2 == null || vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("Vectors must be non-null and of equal length");
        }

        double result = 0.0;
        for (int i = 0; i < vec1.size(); i++) {
            result += vec1.get(i) * vec2.get(i);
        }

        return result;
    }

    /**
     * Calculate magnitude (L2 norm) of a vector
     * @param vector Input vector
     * @return Magnitude
     */
    public static double magnitude(List<Float> vector) {
        if (vector == null || vector.isEmpty()) {
            throw new IllegalArgumentException("Vector must be non-null and non-empty");
        }

        double sum = 0.0;
        for (Float value : vector) {
            sum += value * value;
        }

        return Math.sqrt(sum);
    }
}
