package com.example.demo.dto.response;

import com.example.demo.Enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderUserResponse(
        Long id,
        String orderCode,
        OrderStatus status,
        BigDecimal totalPrice,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        LocalDateTime createdAt
) {
}
