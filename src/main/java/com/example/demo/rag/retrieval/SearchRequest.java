package com.example.demo.rag.retrieval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request for dense retrieval search
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    
    /**
     * Query text to search for
     */
    @NotBlank(message = "query must not be blank")
    private String query;
    
    /**
     * Number of results to return (default: 10)
     */
    @Builder.Default
    @Min(value = 1, message = "limit must be greater than 0")
    private Integer limit = 10;
    
    /**
     * Minimum similarity score threshold (0-1)
     */
    @DecimalMin(value = "0.0", message = "scoreThreshold must be >= 0")
    @DecimalMax(value = "1.0", message = "scoreThreshold must be <= 1")
    private Float scoreThreshold;
    
}
