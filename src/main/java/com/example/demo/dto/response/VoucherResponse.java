package com.example.demo.dto.response;

import com.example.demo.Enums.DiscountType;
import com.example.demo.Enums.VoucherType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VoucherResponse(
        Long id,
        String code,
        VoucherType type,
        DiscountType discountType,
        BigDecimal value,
        BigDecimal minOrderValue,
        BigDecimal maxDiscount,
        Integer quantity,
        Integer usedCount,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean active
) {
}
