package com.example.demo.rag.client;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class ProductQueryDetector {
    // Specific product categories
    private static final List<String> PRODUCT_CATEGORIES = List.of(
            "laptop", "mobile", "phone", "dien thoai", "điện thoại",
            "tv", "television", "tivi", "watch", "dong ho", "đồng hồ",
            "tablet", "may tinh", "máy tính", "camera", "headphone",
            "tai nghe", "loa", "speaker"
    );

    // Product-related action phrases (more specific)
    private static final List<Pattern> PRODUCT_INTENT_PATTERNS = List.of(
            Pattern.compile("\\b(tim|tìm|search|find|show|hien thi|hiển thị)\\s+(san pham|sản phẩm|product)"),
            Pattern.compile("\\b(mua|buy|purchase)\\s+(san pham|sản phẩm|product|laptop|phone|mobile)"),
            Pattern.compile("\\b(gia|giá|price|cost)\\s+(cua|của|of|for)\\s+\\w+"),
            Pattern.compile("\\b(san pham|sản phẩm|product)\\s+(nao|nào|which|what)"),
            Pattern.compile("\\b(co|có|have|available)\\s+(san pham|sản phẩm|product)"),
            Pattern.compile("\\b(khuyen mai|khuy?n m�i|promotion|discount|giam gia|gi?m gi�)"),
            Pattern.compile("\\b(ban chay|b�n ch?y|best selling|top selling|popular|pho bien|ph? bi?n)\\s+(san pham|s?n ph?m|product)?"),
            Pattern.compile("\\b(variant|sku|model)\\s+\\w+")
    );

    public boolean isProductRelatedQuery(String userQuery) {
        if (userQuery == null || userQuery.isBlank()) {
            return false;
        }

        String normalized = userQuery.toLowerCase(Locale.ROOT)
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("đ", "d");

        // Check if query contains specific product categories
        boolean hasProductCategory = PRODUCT_CATEGORIES.stream()
                .anyMatch(normalized::contains);

        // Check if query matches product intent patterns
        boolean hasProductIntent = PRODUCT_INTENT_PATTERNS.stream()
                .anyMatch(pattern -> pattern.matcher(normalized).find());

        return hasProductCategory || hasProductIntent;
    }
}
