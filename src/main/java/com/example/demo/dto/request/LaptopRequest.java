package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LaptopRequest(
        String cpu,
        String ram,
        String storage,
        String gpu,
        String resolution,
        Double screenSize,
        String dimension,
        String colorName,
        String hexCode,
        String image,
        String brand,
        String name,
        String description,
        @NotNull BigDecimal price
) {

}
