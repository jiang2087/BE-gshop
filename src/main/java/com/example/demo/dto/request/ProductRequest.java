package com.example.demo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank String productType,
        @NotBlank String brand,
        String thumbnail,
        @NotBlank String name,
        String description,
        @NotBlank String colorName,
        @NotBlank String hexCode,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
        @NotNull Integer stockQuantity,
        Boolean isDefault,
        Boolean active,
        List<AttributeDTO> attributes
) {
}
