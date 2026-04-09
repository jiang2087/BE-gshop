package com.example.demo.utils;

import com.example.demo.repository.products.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SkuGenerator {
    private final ProductVariantRepository productVariantRepository;

    public String generate(String productName, String hexCode) {
        String baseSku = SkuUtil.normalizedProductName(productName)
                + "-"
                + SkuUtil.normalizedHex(hexCode);
        String sku = baseSku;
        int index = 1;

        while (productVariantRepository.existsBySku(sku)) {
            sku = baseSku + "-" + index++;
        }
        return sku;
    }
}
