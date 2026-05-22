package com.example.demo.rag.tools;

import com.example.demo.dto.product.ProductDetailDto;
import com.example.demo.services.products.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ProductVariantTools {

    private final ProductVariantService productVariantService;

    public ProductDetailDto getProductById(Long productId) {
        return productVariantService.getProductById(productId);
    }

    public Page<ProductDetailDto> getProducts(Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? 10 : Math.min(size, 100);

        String normalizedSortBy = sortBy == null ? "created" : sortBy.trim().toLowerCase(Locale.ROOT);
        String field = switch (normalizedSortBy) {
            case "price" -> "price";
            case "createat", "createdat", "created_at", "created" -> "created";
            default -> "created";
        };

        String normalizedSortDir = sortDir == null ? "desc" : sortDir.trim().toLowerCase(Locale.ROOT);
        Sort.Direction direction = "asc".equals(normalizedSortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return productVariantService.getAllProducts(PageRequest.of(safePage, safeSize, Sort.by(direction, field)));
    }

    public Page<ProductDetailDto> getProductsByPriceRange(List<String> types, BigDecimal minPrice, BigDecimal maxPrice, Integer page, Integer size) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? 10 : Math.min(size, 100);

        PageRequest pageRequest = PageRequest.of(safePage, safeSize);
        
        var productPage = productVariantService.getProductsByPriceRange(types, minPrice, maxPrice, pageRequest);
        
        List<ProductDetailDto> dtos = productPage.getContent().stream()
                .map(product -> productVariantService.getProductById(product.getId()))
                .toList();
        
        return new org.springframework.data.domain.PageImpl<>(
                dtos,
                pageRequest,
                productPage.getTotalElements()
        );
    }
}