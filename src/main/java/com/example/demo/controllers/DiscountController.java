package com.example.demo.controllers;

import com.example.demo.dto.request.DiscountRequest;
import com.example.demo.dto.response.DiscountResponse;
import com.example.demo.models.products.ProductVariant;
import com.example.demo.services.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping
    public ResponseEntity<Page<DiscountResponse>> getAll(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        return ResponseEntity.ok(discountService.getDiscounts(active, name, pageable));
    }

    @PostMapping
    public ResponseEntity<DiscountResponse> create(@RequestBody @Valid DiscountRequest request) {
        return ResponseEntity.ok(discountService.createDiscount(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscountResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid DiscountRequest request
    ) {
        return ResponseEntity.ok(discountService.updateDiscount(id, request));
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<?> applyToProducts(
            @PathVariable Long id,
            @RequestBody List<Long> productVariantIds
    ) {
        discountService.applyDiscountToProducts(id, productVariantIds);
        return ResponseEntity.ok("Applied discount to products");
    }

    @GetMapping("/{id}/variants")
    public ResponseEntity<Page<ProductVariant>> getProductVariantsNotInDiscount(
            @PathVariable Long id,
            @RequestParam(required = false) String sku,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(discountService.getVariantNotInDisCount(id, sku, pageable));
    }

    @GetMapping("/{id}/variants/in")
    public ResponseEntity<Page<ProductVariant>> getVariantInDiscount(
            @PathVariable Long id,
            @RequestParam(required = false) String sku,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(discountService.getVariantInDiscount(id, sku, pageable));
    }

    @GetMapping("/{id}/variants/in/discounted-values")
    public ResponseEntity<Map<Long, Double>> getDiscountedValuesOfVariants(@PathVariable Long id) {
        return ResponseEntity.ok(discountService.getDiscountedValuesOfVariants(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/variants")
    public ResponseEntity<?> addVariantDiscount(
            @PathVariable Long id,
            @RequestBody List<Long> productVariantIds
    ) {
        discountService.addVariantDiscount(id, productVariantIds);
        return ResponseEntity.ok("Added variants to discount");
    }

    @GetMapping("/price")
    public ResponseEntity<?> getDiscountPrice(
            @RequestParam Long productVariantId,
            @RequestParam Double price
    ) {
        Double finalPrice = discountService.calculateDiscountPrice(productVariantId, price);
        return ResponseEntity.ok(finalPrice);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/variants/{variantId}")
    public ResponseEntity<?> deleteVariantInDiscount(
            @PathVariable Long id,
            @PathVariable Long variantId
    ) {
        discountService.deleteVariantInDiscount(id, variantId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.noContent().build();
    }
}
