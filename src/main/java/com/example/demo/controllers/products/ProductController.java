package com.example.demo.controllers.products;

import com.example.demo.dto.request.ProductRequest;
import com.example.demo.dto.request.VariantRequest;
import com.example.demo.dto.response.TopProductProjection;
import com.example.demo.models.Product;
import com.example.demo.services.products.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
        Page<Product> result = productVariantService.getProductsByPriceRange(types, min, max, pageable);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> search(
            @RequestParam(required = false)
            String keyword,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Product> result =
                productVariantService.search(
                        keyword,
                        pageable
                );

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/count")
    public ResponseEntity<?> countProductVariants() {
        return ResponseEntity.ok(productVariantService.countProductVariants());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/top")
    public ResponseEntity<?> getTopProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productVariantService.getTopProducts(page, size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody @Valid ProductRequest productRequest) {
        return ResponseEntity.ok(productVariantService.createProduct(productRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id ,@RequestBody @Valid ProductRequest productRequest) {
        return ResponseEntity.ok(productVariantService.updateProduct(id,  productRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/variants")
    public ResponseEntity<?> createVariant(@PathVariable Long id, @RequestBody @Valid VariantRequest request) {
        return ResponseEntity.ok(productVariantService.createVariant(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/variants/{variantId}")
    public ResponseEntity<?> updateVariant(@PathVariable Long variantId, @RequestBody @Valid VariantRequest request) {
        return ResponseEntity.ok(productVariantService.updateVariant(variantId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<?> deleteVariant(@PathVariable Long variantId) {
        productVariantService.deleteVariant(variantId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProductById(@PathVariable long id) {
        productVariantService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
