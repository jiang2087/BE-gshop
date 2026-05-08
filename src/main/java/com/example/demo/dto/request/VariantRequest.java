package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VariantRequest (
        @NotNull BigDecimal price,
        String hexCode,
        String colorName,
        String image,
        Integer stock,
        Boolean isDefault
){

}
