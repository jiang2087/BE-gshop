package com.example.demo.rag.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ToolSelector {
    private static final String TOOL_GET_PRODUCT_BY_ID = "get_product_by_id";
    private static final String TOOL_GET_PRODUCTS = "get_products";
    private static final String TOOL_GET_PRODUCTS_BY_PRICE_RANGE = "get_products_by_price_range";
    private static final String TOOL_GET_TOP_PURCHASERS = "get_top_purchasers";
    private static final String TOOL_GET_TOP_PRODUCTS_BY_PURCHASE_COUNT = "get_top_products_by_purchase_count";
    private static final String TOOL_GET_MOST_PURCHASED_PRODUCTS = "get_most_purchased_products";
    private static final double HIGH_CONFIDENCE = 0.9;
    private static final double MEDIUM_CONFIDENCE = 0.7;
    private static final double LOW_CONFIDENCE = 0.3;
    private static final BigDecimal DEFAULT_MAX_PRICE = new BigDecimal("999999999");

    @Value("${chat.client.vnd-to-usd-rate:26000}")
    private BigDecimal vndToUsdRate;

    public ToolSelection selectTool(String userQuery, List<Map<String, Object>> tools) {
        if (userQuery == null || userQuery.isBlank() || tools == null || tools.isEmpty()) {
            return null;
        }

        String normalized = userQuery.toLowerCase(Locale.ROOT);
        
        ToolSelection productByIdSelection = checkProductById(normalized);
        if (productByIdSelection != null) {
            return productByIdSelection;
        }
        
        ToolSelection priceRangeSelection = checkProductsByPriceRange(normalized);
        if (priceRangeSelection != null) {
            return priceRangeSelection;
        }
        
        ToolSelection topPurchasersSelection = checkTopPurchasers(normalized);
        if (topPurchasersSelection != null) {
            return topPurchasersSelection;
        }
        
        ToolSelection topProductsSelection = checkTopProductsByPurchaseCount(normalized);
        if (topProductsSelection != null) {
            return topProductsSelection;
        }
        
        ToolSelection mostPurchasedSelection = checkMostPurchasedProducts(normalized);
        if (mostPurchasedSelection != null) {
            return mostPurchasedSelection;
        }

        ToolSelection productsListSelection = checkProductsList(normalized);
        if (productsListSelection != null) {
            return productsListSelection;
        }

        return null;
    }

    private ToolSelection checkProductById(String normalized) {
        Pattern productIdPattern = Pattern.compile("\\b(product[_\\s]?id|id)[:\\s]*(\\d+)\\b");
        Matcher matcher = productIdPattern.matcher(normalized);

        if (matcher.find()) {
            Long productId = Long.parseLong(matcher.group(2));
            Map<String, Object> args = new HashMap<>();
            args.put("productId", productId);

            return ToolSelection.builder()
                    .toolName(TOOL_GET_PRODUCT_BY_ID)
                    .arguments(args)
                    .confidence(HIGH_CONFIDENCE)
                    .shouldUseDirectExecution(true)
                    .build();
        }

        return null;
    }

    private ToolSelection checkProductsByPriceRange(String normalized) {
        Pattern priceRangePattern = Pattern.compile(
            ".*(price|gia|giá).*(range|between|from|tu|từ|trong khoang|trong khoảng).*|" +
            ".*(under|below|duoi|dưới|less than|cheaper than).*(\\$|dollar|usd|vnd|dong|đ).*|" +
            ".*(over|above|tren|trên|more than|greater than|expensive than).*(\\$|dollar|usd|vnd|dong|đ).*|" +
            ".*(between|from).*(\\d+).*(to|and|den|đến).*(\\d+).*"
        );
        
        if (priceRangePattern.matcher(normalized).matches()) {
            Map<String, Object> args = new HashMap<>();
            boolean isVndQuery = isVndQuery(normalized);
            PriceRange priceRange = extractPriceRange(normalized, isVndQuery);
            
            if (priceRange != null) {
                args.put("minPrice", priceRange.min);
                args.put("maxPrice", priceRange.max);
                
                List<String> types = extractProductTypes(normalized);
                if (!types.isEmpty()) {
                    args.put("types", types);
                }
                
                Map<String, Object> paginationArgs = extractPaginationArgs(normalized);
                args.putAll(paginationArgs);
                
                return ToolSelection.builder()
                        .toolName(TOOL_GET_PRODUCTS_BY_PRICE_RANGE)
                        .arguments(args)
                        .confidence(HIGH_CONFIDENCE)
                        .shouldUseDirectExecution(true)
                        .build();
            }
        }
        
        return null;
    }

    private PriceRange extractPriceRange(String normalized, boolean isVndQuery) {
        Pattern betweenPattern = Pattern.compile(
            "\\b(between|from|tu|từ)[\\s:]*([\\d,.]+)[\\s]*(k|tr|trieu|triệu)?[\\s]*(\\$|usd|dollar|vnd|dong|đ)?[\\s]*(to|and|den|đến)[\\s:]*([\\d,.]+)[\\s]*(k|tr|trieu|triệu)?\\b"
        );
        Matcher betweenMatcher = betweenPattern.matcher(normalized);
        if (betweenMatcher.find()) {
            BigDecimal min = parsePriceValue(betweenMatcher.group(2), betweenMatcher.group(3), isVndQuery);
            BigDecimal max = parsePriceValue(betweenMatcher.group(6), betweenMatcher.group(7), isVndQuery);
            return new PriceRange(min, max);
        }
        
        Pattern underPattern = Pattern.compile(
            "\\b(under|below|less than|cheaper than|duoi|dưới)[\\s:]*([\\d,.]+)[\\s]*(k|tr|trieu|triệu)?[\\s]*(\\$|usd|dollar|vnd|dong|đ)?\\b"
        );
        Matcher underMatcher = underPattern.matcher(normalized);
        if (underMatcher.find()) {
            BigDecimal max = parsePriceValue(underMatcher.group(2), underMatcher.group(3), isVndQuery);
            return new PriceRange(BigDecimal.ZERO, max);
        }
        
        Pattern overPattern = Pattern.compile(
            "\\b(over|above|more than|greater than|expensive than|tren|trên)[\\s:]*([\\d,.]+)[\\s]*(k|tr|trieu|triệu)?[\\s]*(\\$|usd|dollar|vnd|dong|đ)?\\b"
        );
        Matcher overMatcher = overPattern.matcher(normalized);
        if (overMatcher.find()) {
            BigDecimal min = parsePriceValue(overMatcher.group(2), overMatcher.group(3), isVndQuery);
            return new PriceRange(min, DEFAULT_MAX_PRICE);
        }
        
        Pattern rangePattern = Pattern.compile("\\b([\\d,.]+)[\\s]*(k|tr|trieu|triệu)?[\\s]*[-–—][\\s]*([\\d,.]+)[\\s]*(k|tr|trieu|triệu)?\\b");
        Matcher rangeMatcher = rangePattern.matcher(normalized);
        if (rangeMatcher.find()) {
            BigDecimal min = parsePriceValue(rangeMatcher.group(1), rangeMatcher.group(2), isVndQuery);
            BigDecimal max = parsePriceValue(rangeMatcher.group(3), rangeMatcher.group(4), isVndQuery);
            return new PriceRange(min, max);
        }
        
        return null;
    }

    private BigDecimal parsePriceValue(String value, String suffix, boolean isVndQuery) {
        String cleaned = value.replaceAll("[,\\s]", "");
        BigDecimal parsed = new BigDecimal(cleaned);
        if ("k".equals(suffix)) {
            parsed = parsed.multiply(new BigDecimal("1000"));
        } else if ("tr".equals(suffix) || "trieu".equals(suffix) || "triệu".equals(suffix)) {
            parsed = parsed.multiply(new BigDecimal("1000000"));
        }
        if (!isVndQuery) {
            return parsed;
        }
        return parsed.divide(vndToUsdRate, 2, RoundingMode.HALF_UP);
    }

    private boolean isVndQuery(String normalized) {
        return containsAny(normalized, "vnd", "vnđ", "dong", "đ");
    }

    private List<String> extractProductTypes(String normalized) {
        List<String> types = new ArrayList<>();
        
        if (containsAny(normalized, "mobile", "phone", "smartphone", "dien thoai", "điện thoại")) {
            types.add("MOBILE");
        }
        if (containsAny(normalized, "laptop", "notebook", "may tinh", "máy tính")) {
            types.add("LAPTOP");
        }
        if (containsAny(normalized, "television", "tv", "tivi", "ti vi")) {
            types.add("TELEVISION");
        }
        if (containsAny(normalized, "watch", "smartwatch", "dong ho", "đồng hồ")) {
            types.add("WATCHES");
        }
        
        return types;
    }

    private ToolSelection checkTopPurchasers(String normalized) {
        Pattern topPurchasersPattern = Pattern.compile(
            ".*(top|best|highest|biggest|leading).*" +
            "(customer|purchaser|buyer|user|client|spending|spender).*"
        );
        
        if (topPurchasersPattern.matcher(normalized).matches()) {
            Map<String, Object> args = new HashMap<>();
            
            Pattern limitPattern = Pattern.compile("\\b(top|best|first|limit|\\d+)[:\\s]*(\\d+)\\b");
            Matcher limitMatcher = limitPattern.matcher(normalized);
            if (limitMatcher.find()) {
                args.put("limit", Integer.parseInt(limitMatcher.group(2)));
            }
            
            return ToolSelection.builder()
                    .toolName(TOOL_GET_TOP_PURCHASERS)
                    .arguments(args)
                    .confidence(HIGH_CONFIDENCE)
                    .shouldUseDirectExecution(true)
                    .build();
        }
        
        return null;
    }

    private ToolSelection checkTopProductsByPurchaseCount(String normalized) {
        Pattern topProductsPattern = Pattern.compile(
            ".*(ban chay|bán chạy|selling|sold|top.*product|best.*product|most.*product).*",
            Pattern.CASE_INSENSITIVE
        );
        
        if (topProductsPattern.matcher(normalized).matches()) {
            Map<String, Object> args = new HashMap<>();
            
            Pattern numberPattern = Pattern.compile("\\b(\\d+)\\b");
            Matcher numberMatcher = numberPattern.matcher(normalized);
            if (numberMatcher.find()) {
                args.put("limit", Integer.parseInt(numberMatcher.group(1)));
            }
            
            return ToolSelection.builder()
                    .toolName(TOOL_GET_TOP_PRODUCTS_BY_PURCHASE_COUNT)
                    .arguments(args)
                    .confidence(HIGH_CONFIDENCE)
                    .shouldUseDirectExecution(true)
                    .build();
        }
        
        return null;
    }

    private ToolSelection checkMostPurchasedProducts(String normalized) {
        Pattern mostPurchasedPattern = Pattern.compile(
            ".*(most|highest|best).*(purchased|bought|revenue|sales|selling).*" +
            "(product|item).*"
        );
        
        if (mostPurchasedPattern.matcher(normalized).matches()) {
            Map<String, Object> args = new HashMap<>();
            
            Pattern limitPattern = Pattern.compile("\\b(top|best|first|limit)[:\\s]*(\\d+)\\b");
            Matcher limitMatcher = limitPattern.matcher(normalized);
            if (limitMatcher.find()) {
                args.put("limit", Integer.parseInt(limitMatcher.group(2)));
            }
            
            return ToolSelection.builder()
                    .toolName(TOOL_GET_MOST_PURCHASED_PRODUCTS)
                    .arguments(args)
                    .confidence(HIGH_CONFIDENCE)
                    .shouldUseDirectExecution(true)
                    .build();
        }
        
        return null;
    }

    private ToolSelection checkProductsList(String normalized) {
        Pattern listPattern = Pattern.compile(".*(list|browse|top|many|all|show me).*product.*");
        if (listPattern.matcher(normalized).matches()) {
            Map<String, Object> args = extractPaginationArgs(normalized);

            return ToolSelection.builder()
                    .toolName(TOOL_GET_PRODUCTS)
                    .arguments(args)
                    .confidence(MEDIUM_CONFIDENCE)
                    .shouldUseDirectExecution(true)
                    .build();
        }

        return null;
    }

    private Map<String, Object> extractPaginationArgs(String normalized) {
        Map<String, Object> args = new HashMap<>();
        
        Pattern pagePattern = Pattern.compile("\\bpage[:\\s]*(\\d+)\\b");
        Matcher pageMatcher = pagePattern.matcher(normalized);
        if (pageMatcher.find()) {
            args.put("page", Integer.parseInt(pageMatcher.group(1)));
        }

        Pattern sizePattern = Pattern.compile("\\b(size|limit|top)[:\\s]*(\\d+)\\b");
        Matcher sizeMatcher = sizePattern.matcher(normalized);
        if (sizeMatcher.find()) {
            args.put("size", Integer.parseInt(sizeMatcher.group(2)));
        }

        applySortingArgs(normalized, args);

        return args;
    }

    private void applySortingArgs(String normalized, Map<String, Object> args) {
        boolean priceSort = normalized.contains("price")
                || normalized.contains("gia")
                || normalized.contains("giá");
        boolean createdSort = normalized.contains("created")
                || normalized.contains("newest")
                || normalized.contains("latest")
                || normalized.contains("new")
                || normalized.contains("moi nhat")
                || normalized.contains("mới nhất");

        if (priceSort) {
            args.put("sortBy", "price");
            if (containsAny(normalized, "asc", "low to high", "cheapest", "thap den cao", "thấp đến cao")) {
                args.put("sortDir", "asc");
            } else if (containsAny(normalized, "desc", "high to low", "expensive", "cao den thap", "cao đến thấp")) {
                args.put("sortDir", "desc");
            }
            return;
        }

        if (createdSort) {
            args.put("sortBy", "createdAt");
            if (containsAny(normalized, "asc", "oldest", "cu nhat", "cũ nhất")) {
                args.put("sortDir", "asc");
            } else if (containsAny(normalized, "desc", "newest", "latest", "moi nhat", "mới nhất")) {
                args.put("sortDir", "desc");
            }
        }
    }

    private boolean containsAny(String input, String... keys) {
        for (String key : keys) {
            if (input.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private static class PriceRange {
        final BigDecimal min;
        final BigDecimal max;

        PriceRange(BigDecimal min, BigDecimal max) {
            this.min = min;
            this.max = max;
        }
    }
}
