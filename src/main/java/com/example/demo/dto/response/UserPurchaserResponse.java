package com.example.demo.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserPurchaserResponse(
        Long userId,
        String username,
        String email,
        BigDecimal totalPurchased,
        LocalDateTime lastPurchase
) {
}
