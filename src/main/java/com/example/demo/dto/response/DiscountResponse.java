package com.example.demo.dto.response;

import com.example.demo.Enums.DiscountType;

import java.time.LocalDateTime;

public record DiscountResponse(
        Long id,
        String name,
        DiscountType type,
        Double value,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean active
) {
}
