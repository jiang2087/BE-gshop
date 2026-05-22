package com.example.demo.services.ai;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;


@Data
@Builder
public class SearchOptions {

    @Builder.Default
    private int limit = 10;


    @Builder.Default
    private float scoreThreshold = 0.0f;


    @Builder.Default
    private int offset = 0;


    @Builder.Default
    private boolean withPayload = true;


    @Builder.Default
    private boolean withVector = false;


    @Builder.Default
    private Map<String, Object> filters = new HashMap<>();


    @Builder.Default
    private long timeoutMs = 5000;


    public SearchOptions addFilter(String field, Object value) {
        this.filters.put(field, value);
        return this;
    }


    public static SearchOptions defaults() {
        return SearchOptions.builder().build();
    }


    public static SearchOptions withLimit(int limit) {
        return SearchOptions.builder()
                .limit(limit)
                .build();
    }

    public static SearchOptions withThreshold(float threshold) {
        return SearchOptions.builder()
                .scoreThreshold(threshold)
                .build();
    }


    public static SearchOptions withLimitAndThreshold(int limit, float threshold) {
        return SearchOptions.builder()
                .limit(limit)
                .scoreThreshold(threshold)
                .build();
    }
}
