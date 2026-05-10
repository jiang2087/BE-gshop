package com.example.demo.dto.request;

import com.example.demo.Enums.DiscountType;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record DiscountRequest(

    @NotBlank
    String name,

    @NotNull
    DiscountType type,

    @NotNull
    @Positive
    Double value,

    @NotNull
    LocalDateTime startDate,

    @NotNull
    LocalDateTime endDate,

    Boolean active

) {}
