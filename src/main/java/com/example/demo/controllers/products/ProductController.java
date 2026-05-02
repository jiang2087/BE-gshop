package com.example.demo.controllers.products;

import com.example.demo.models.Product;
import com.example.demo.services.products.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductVariantService productVariantService;

    @GetMapping
    public ResponseEntity<?> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productVariantService.getAllProducts(pageable));
    }

    @GetMapping("/names")
    public ResponseEntity<?> getNameByIds(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(productVariantService.getNameByIds(ids));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable long id) {
        return ResponseEntity.ok(productVariantService.getProductById(id));
    }

    @GetMapping("/type")
    public ResponseEntity<?> getProductByType(@RequestParam List<String> types, Pageable pageable) {
        return ResponseEntity.ok(productVariantService.getProductByType(types, pageable));
    }

    @GetMapping("/type/count")
    public ResponseEntity<?> getProductTypeCount(@RequestParam List<String> types) {
        return ResponseEntity.ok(
                productVariantService.countProductsByType(types)
        );
    }

    @GetMapping("/price-range")
    public ResponseEntity<Page<Product>> getProductsByPriceRange(
            @RequestParam(required = false) List<String> types,
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            Pageable pageable
    ) {
        Page<Product> result = productVariantService.getProductsByPriceRange(types,min, max, pageable);
        return ResponseEntity.ok(result);
    }
}
