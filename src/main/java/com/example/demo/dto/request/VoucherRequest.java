package com.example.demo.dto.request;

import com.example.demo.Enums.DiscountType;
import com.example.demo.Enums.VoucherType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VoucherRequest(

    @NotBlank
    String code,

    @NotNull
    VoucherType type,

    @NotNull
    DiscountType discountType,

    @NotNull
    @Positive
    BigDecimal value,

    BigDecimal minOrderValue,

    BigDecimal maxDiscount,

    @NotNull
    @Positive
    Integer quantity,

    @NotNull
    LocalDateTime startDate,

    @NotNull
    LocalDateTime endDate,

    Boolean active

) {}