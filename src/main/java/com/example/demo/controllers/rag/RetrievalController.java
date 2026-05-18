package com.example.demo.controllers.rag;

import com.example.demo.rag.retrieval.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for dense retrieval operations
 */
@Slf4j
@RestController
@RequestMapping("/api/retrieval")
@RequiredArgsConstructor
@Validated
public class RetrievalController {

    private final DenseRetrievalService retrievalService;

    /**
     * Search for products using dense retrieval
     * POST /api/retrieval/search
     */
    @PostMapping("/search")
    public ResponseEntity<SearchResponse> search(@Valid @RequestBody SearchRequest request) {
        log.info("Received search request: query={}, limit={}", 
                request.getQuery(), request.getLimit());
        
        SearchResponse response = retrievalService.search(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Simple search with query parameter
     * GET /api/retrieval/search?q=laptop&limit=10
     */
    @GetMapping("/search")
    public ResponseEntity<List<SearchResult>> searchSimple(
            @RequestParam("q") @NotBlank String query,
            @RequestParam(value = "limit", defaultValue = "10") @Min(1) int limit
    ) {
        log.info("Received simple search: query={}, limit={}", query, limit);
        
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .limit(limit)
                .build();
        
        List<SearchResult> results = retrievalService.search(request).getResults();
        return ResponseEntity.ok(results);
    }

    /**
     * Health check endpoint
     * GET /api/retrieval/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new HealthResponse("ok", "Dense retrieval service is running"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RetrievalException.class)
    public ResponseEntity<Map<String, String>> handleRetrievalError(RetrievalException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Retrieval operation failed"));
    }

    /**
     * Health response DTO
     */
    private record HealthResponse(String status, String message) {}
}
