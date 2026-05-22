package com.example.demo.rag.tools.implementations;

import com.example.demo.rag.tools.ProductVariantTools;
import com.example.demo.rag.tools.spec.BaseTool;
import com.example.demo.rag.tools.spec.ToolHandler;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetProductsByPriceRangeTool extends BaseTool implements ToolHandler {

    private final ProductVariantTools productVariantTools;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "get_products_by_price_range";
    }

    @Override
    public String getDescription() {
        return "Use when user asks for products within a specific price range. Supports filtering by product types and pagination.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "types", Map.of(
                    "type", "array",
                    "items", Map.of("type", "string"),
                    "description", "Optional list of product types (e.g., MOBILE, LAPTOP, TELEVISION, WATCHES)"
                ),
                "minPrice", Map.of(
                    "type", "number",
                    "description", "Minimum price (must be non-negative)"
                ),
                "maxPrice", Map.of(
                    "type", "number",
                    "description", "Maximum price (must be >= minPrice)"
                ),
                "page", Map.of(
                    "type", "integer",
                    "description", "Page number (0-indexed)"
                ),
                "size", Map.of(
                    "type", "integer",
                    "description", "Page size (max 100)"
                )
            ),
            "required", List.of("minPrice", "maxPrice")
        );
    }

    @Override
    public String execute(Map<String, Object> arguments) throws Exception {
        List<String> types = arguments.get("types") == null ? null : (List<String>) arguments.get("types");
        BigDecimal minPrice = new BigDecimal(arguments.get("minPrice").toString());
        BigDecimal maxPrice = new BigDecimal(arguments.get("maxPrice").toString());
        Integer page = arguments.get("page") == null ? null : ((Number) arguments.get("page")).intValue();
        Integer size = arguments.get("size") == null ? null : ((Number) arguments.get("size")).intValue();
        
        Object result = productVariantTools.getProductsByPriceRange(types, minPrice, maxPrice, page, size);
        return objectMapper.writeValueAsString(result);
    }
}