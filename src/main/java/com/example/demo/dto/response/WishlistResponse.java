package com.example.demo.dto.response;

import java.math.BigDecimal;

public record WishlistResponse (
   Long productVariantId,
   Long productId,
   Long userId,
   String thumbnail,
   BigDecimal price,
   String name,
   boolean inStock
){ }
