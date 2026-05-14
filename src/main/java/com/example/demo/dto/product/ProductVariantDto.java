package com.example.demo.dto.product;

import com.example.demo.Enums.DiscountType;

public record ProductVariantDto(
        Long id,
        String sku,
        Double originalPrice,
        Integer stockQuantity,
        Boolean active,
        Boolean isDefault,
        String image,
        Integer version,
        ColorDto color,
        Double discountedPrice,
        DiscountType discountType
) {
}