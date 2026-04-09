package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
    @NotNull(message = "user id can not null") Long userId,
    @NotNull(message = "product id can not nul")  Long productVariantId,
    @NotNull(message = "comment can not null") String comment,
    @NotNull(message = "ohh you forgot enter the name!") String username,
    String avatar,
    Integer rating
) { }
