package com.example.demo.dto.response;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CartItemResponse(
        Long cartId,
        Long productVariantId,
        String imageUrl,
        String sku,
        String hexColor,
        String nameColor,
        BigDecimal price,
        Integer quantity,
        @NotNull(message = "cartKey can not null") String cartKey

) {
}
