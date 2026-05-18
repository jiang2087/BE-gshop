package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPurchaseResponse {
    private Long productId;
    private String productName;
    private Long totalQuantitySold;
    private Long orderCount;
    private BigDecimal totalRevenue;
}
