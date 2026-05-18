package com.example.demo.rag.retrieval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response from dense retrieval search
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    
    /**
     * Original query text
     */
    private String query;
    
    /**
     * List of search results
     */
    private List<SearchResult> results;
    
    /**
     * Total number of results found
     */
    private Integer totalResults;
    
    /**
     * Search execution time in milliseconds
     */
    private Long executionTimeMs;
    
    /**
     * Filters applied to the search
     */
    private Map<String, Object> appliedFilters;
    
    /**
     * Whether results were truncated by limit
     */
    private Boolean truncated;
}
