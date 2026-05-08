package com.example.demo.dto.response;



public interface TopProductProjection {
    String getImage();
    String getProductName();
    String getProductType();
    Double getPrice();
    Long getSold();
    Double getProfit();
}