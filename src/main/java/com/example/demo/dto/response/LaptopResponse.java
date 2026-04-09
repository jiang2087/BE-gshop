package com.example.demo.dto.response;

import java.math.BigDecimal;

public record LaptopResponse(
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
        String sku,
        BigDecimal price
) {

}
