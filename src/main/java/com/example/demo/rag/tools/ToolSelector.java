package com.example.demo.rag.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
    private static final String TOOL_GET_TOP_PURCHASERS = "get_top_purchasers";
    private static final String TOOL_GET_TOP_PRODUCTS_BY_PURCHASE_COUNT = "get_top_products_by_purchase_count";
    private static final String TOOL_GET_MOST_PURCHASED_PRODUCTS = "get_most_purchased_products";
    private static final double HIGH_CONFIDENCE = 0.9;
    private static final double MEDIUM_CONFIDENCE = 0.7;
    private static final double LOW_CONFIDENCE = 0.3;

    public ToolSelection selectTool(String userQuery, List<Map<String, Object>> tools) {
        if (userQuery == null || userQuery.isBlank() || tools == null || tools.isEmpty()) {
            return null;
        }

        String normalized = userQuery.toLowerCase(Locale.ROOT);
        
        ToolSelection productByIdSelection = checkProductById(normalized);
        if (productByIdSelection != null) {
            return productByIdSelection;
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
            ".*(ban chay|selling|sold|top.*product|best.*product|most.*product).*",
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
}
