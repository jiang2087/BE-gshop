package com.example.demo.rag.semantic;

import com.example.demo.dto.product.ProductDetailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service to dynamically build semantic text for products
 * based on their product type
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticBuilderService {

    private final List<ProductSemanticBuilder> builders;


    public String buildSemanticText(ProductDetailDto product) {
        if (product == null) {
            log.warn("Null product provided to buildSemanticText");
            return "";
        }

        String productType = product.productType();
        if (productType == null || productType.trim().isEmpty()) {
            log.warn("Product {} has no product type", product.id());
            return buildDefaultSemanticText(product);
        }

        Optional<ProductSemanticBuilder> builder = builders.stream()
                .filter(b -> b.supports(productType))
                .findFirst();

        if (builder.isPresent()) {
            log.debug("Building semantic text for product {} with type {}", 
                    product.id(), productType);
            return builder.get().build(product);
        } else {
            log.warn("No builder found for product type: {}, using default builder", productType);
            return buildDefaultSemanticText(product);
        }
    }


    public List<String> buildSemanticTextBatch(List<ProductDetailDto> products) {
        if (products == null || products.isEmpty()) {
            log.warn("Empty product list provided to buildSemanticTextBatch");
            return List.of();
        }

        return products.stream()
                .map(this::buildSemanticText)
                .toList();
    }


    public boolean hasBuilderFor(String productType) {
        if (productType == null || productType.trim().isEmpty()) {
            return false;
        }

        return builders.stream()
                .anyMatch(b -> b.supports(productType));
    }


    public List<String> getSupportedProductTypes() {
        return List.of("LAPTOP", "MOBILE", "WATCHES", "TELEVISION");
    }


    private String buildDefaultSemanticText(ProductDetailDto product) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%s là sản phẩm của %s.\n\n", 
                product.name(), product.brand()));

        sb.append("Mô tả:\n");
        sb.append(product.description()).append("\n\n");

        if (product.productAttributes() != null && !product.productAttributes().isEmpty()) {
            sb.append("Thông số:\n");
            product.productAttributes().forEach((key, value) -> {
                if (value != null && !value.toString().trim().isEmpty()) {
                    sb.append(key).append(": ").append(value).append("\n");
                }
            });
        }

        return sb.toString();
    }
}
