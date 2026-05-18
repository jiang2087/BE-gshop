package com.example.demo.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        String productName,
        String image,
        String sku,
        Integer quantity,
        BigDecimal price,
        BigDecimal subtotal
) {
}
