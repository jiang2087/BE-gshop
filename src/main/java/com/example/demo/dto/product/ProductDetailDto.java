package com.example.demo.dto.product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ProductDetailDto(
        Long id,
        String name,
        String brand,
        String description,
        String productType,
        String thumbnail,
        LocalDateTime created,
        Map<String, Object> productAttributes,
        List<ProductVariantDto> productVariants
) {
}
