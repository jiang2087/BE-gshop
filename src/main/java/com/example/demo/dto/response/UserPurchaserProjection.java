package com.example.demo.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface UserPurchaserProjection {
    Long getUserId();
    String getUsername();
    String getEmail();
    BigDecimal getTotalPurchased();
    LocalDateTime getLastPurchase();
}
