package com.example.demo.dto.response;

public record ReviewStats(Long productId, Long count, Double avg) {
    public Double avgDouble() {
        return avg == null ? 0.0 : avg.doubleValue();
    }
}
