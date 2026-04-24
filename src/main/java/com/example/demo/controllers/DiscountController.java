package com.example.demo.controllers;

import com.example.demo.dto.request.DiscountRequest;
import com.example.demo.services.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid DiscountRequest request) {
        return ResponseEntity.ok(discountService.createDiscount(request));
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<?> applyToProducts(
            @PathVariable Long id,
            @RequestBody List<Long> productVariantIds
    ) {
        discountService.applyDiscountToProducts(id, productVariantIds);
        return ResponseEntity.ok("Applied discount to products");
    }

    @GetMapping("/price")
    public ResponseEntity<?> getDiscountPrice(
            @RequestParam Long productVariantId,
            @RequestParam Double price
    ) {
        Double finalPrice = discountService.calculateDiscountPrice(productVariantId, price);
        return ResponseEntity.ok(finalPrice);
    }
}