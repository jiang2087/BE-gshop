package com.example.demo.controllers.rag;

import com.example.demo.dto.product.ProductDetailDto;
import com.example.demo.rag.ingestion.IngestionDocument;
import com.example.demo.rag.ingestion.IngestionService;
import com.example.demo.services.products.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rag/ingestion")
public class IngestionController {

    private final IngestionService ingestionService;
    private final ProductVariantService productVariantService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/products/{productId}")
    public ResponseEntity<List<IngestionDocument>> ingestProduct(@PathVariable Long productId) {
        ProductDetailDto product = productVariantService.getProductById(productId);
        return ResponseEntity.ok(ingestionService.ingestProduct(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/products/batch")
    public ResponseEntity<List<IngestionDocument>> ingestBatch(@RequestParam List<Long> productIds) {
        Map<Long, ProductDetailDto> productsById = productVariantService.getProductsByIds(productIds);
        List<ProductDetailDto> products = productIds.stream()
                .map(productsById::get)
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(ingestionService.ingestBatch(products));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/products/{productId}/stats")
    public ResponseEntity<IngestionService.IngestionStats> getIngestionStats(@PathVariable Long productId) {
        ProductDetailDto product = productVariantService.getProductById(productId);
        return ResponseEntity.ok(ingestionService.getIngestionStats(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteProductFromVectorDb(@PathVariable Long productId) {
        ingestionService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/collection")
    public ResponseEntity<Void> clearCollection(@RequestParam(required = false) String name) {
        if (name == null || name.isBlank()) {
            ingestionService.clearCollection();
        } else {
            ingestionService.clearCollection(name);
        }
        return ResponseEntity.noContent().build();
    }
}
