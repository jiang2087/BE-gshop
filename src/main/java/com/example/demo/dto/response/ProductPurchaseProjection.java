package com.example.demo.dto.response;

import java.math.BigDecimal;

public interface ProductPurchaseProjection {
    Long getProductId();
    String getProductName();
    Long getTotalQuantitySold();
    Long getOrderCount();
    BigDecimal getTotalRevenue();
}
