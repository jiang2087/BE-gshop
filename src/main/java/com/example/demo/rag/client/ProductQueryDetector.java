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
            Pattern.compile("\\b(tim|tìm|search|find|show|hien thi|hiển thị|cho toi xem|cho tôi xem)\\s+(san pham|sản phẩm|product)"),
            Pattern.compile("\\b(mua|buy|purchase)\\s+(san pham|sản phẩm|product|laptop|phone|mobile)"),
            Pattern.compile("\\b(gia|giá|price|cost)\\s+(cua|của|of|for)\\s+\\w+"),
            Pattern.compile("\\b(san pham|sản phẩm|product)\\s+(nao|nào|which|what)"),
            Pattern.compile("\\b(co|có|have|available)\\s+(san pham|sản phẩm|product)"),
            Pattern.compile("\\b(khuyen mai|khuyến mãi|promotion|discount|giam gia|giảm giá)"),
            Pattern.compile("\\b(ban chay|bán chạy|best selling|top selling|popular|pho bien|phổ biến)\\s+(san pham|sản phẩm|product)?"),
            Pattern.compile("\\b(variant|sku|model)\\s+\\w+"),
            // Price-related patterns
            Pattern.compile("\\b(dat nhat|đắt nhất|most expensive|highest price|gia cao nhat|giá cao nhất)"),
            Pattern.compile("\\b(re nhat|rẻ nhất|cheapest|lowest price|gia thap nhat|giá thấp nhất)"),
            Pattern.compile("\\b(gia|giá|price)\\s+(cao|thap|thấp|high|low)"),
            Pattern.compile("\\b(sap xep|sắp xếp|sort|order)\\s+(theo|by)\\s+(gia|giá|price)"),
            Pattern.compile("\\b(san pham|sản phẩm|product)\\s+(dat|đắt|re|rẻ|expensive|cheap)")
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
