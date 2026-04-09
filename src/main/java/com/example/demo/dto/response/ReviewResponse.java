package com.example.demo.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        String username,
        String comment,
        LocalDateTime createdAt,
        Integer rating,
        String avatar,
        Long orderId,
        Long userId,
        Long productId,
        LocalDateTime updateAt,
        Integer helpfulCount
) { }
