package com.example.demo.rag.tools.implementations;

import com.example.demo.services.OrderService;
import com.example.demo.rag.tools.spec.BaseTool;
import com.example.demo.rag.tools.spec.ToolHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetMostPurchasedProductsTool extends BaseTool implements ToolHandler {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "get_most_purchased_products";
    }

    @Override
    public String getDescription() {
        return "Use when user asks for most purchased products with detailed revenue information. Returns products with quantity sold, order count, and total revenue.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "limit", Map.of(
                    "type", "integer",
                    "description", "Number of top products to return (default: 10)"
                )
            )
        );
    }

    @Override
    public String execute(Map<String, Object> arguments) throws Exception {
        Integer limit = arguments.get("limit") == null ? null : ((Number) arguments.get("limit")).intValue();
        Object result = orderService.getMostPurchasedProducts(limit);
        return objectMapper.writeValueAsString(result);
    }
}
