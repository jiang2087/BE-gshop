package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddressRequest(@NotNull String recipientName, @NotNull String phone, @NotNull String address) {
}
