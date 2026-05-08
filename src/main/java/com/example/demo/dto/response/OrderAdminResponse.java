package com.example.demo.dto.response;

import com.example.demo.Enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderAdminResponse(
        Long id,
        String orderCode,
        String customerName,
        String phone,
        BigDecimal totalPrice,
        String paymentMethod,
        OrderStatus status,
        LocalDateTime createdAt
) {
}