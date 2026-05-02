package com.example.demo.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record PlaceOrderRequest(

        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be positive")
        Long userId,

        @NotNull(message = "Delivery address ID is required")
        @Positive(message = "Address ID must be positive")
        Long addressId,

        @NotEmpty(message = "Order must contain at least one item")
        @Size(min = 1, max = 50, message = "Maximum 50 items per order")
        List<@Valid OrderItemRequest> items,

        String paymentMethod,       // "COD", "VNPAY", "MOMO", "BANK_CARD"

        String note,

        String voucherCode,

        Integer shippingFee

) {
    // Nested record
    public record OrderItemRequest(

            @NotNull(message = "Product variant ID is required")
            @Positive(message = "Variant ID must be positive")
            Long variantId,

            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            @Max(value = 100, message = "Maximum 100 items per variant") // tùy business
            Integer quantity

    ) {}
}