package com.example.demo.rag.tools.implementations;

import com.example.demo.services.OrderService;
import com.example.demo.rag.tools.spec.BaseTool;
import com.example.demo.rag.tools.spec.ToolHandler;
import com.example.demo.rag.tools.spec.ToolRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetTopPurchasersTool extends BaseTool implements ToolHandler {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "get_top_purchasers";
    }

    @Override
    public String getDescription() {
        return "Use when user asks for top customers, best buyers, or highest spending users. Returns users ranked by total purchase amount.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "limit", Map.of(
                    "type", "integer",
                    "description", "Number of top purchasers to return (default: 10)"
                )
            )
        );
    }

    @Override
    public ToolRole getRequiredRole() {
        return ToolRole.ADMIN;
    }

    @Override
public String execute(Map<String, Object> arguments) throws Exception {
        Integer limit = arguments.get("limit") == null ? null : ((Number) arguments.get("limit")).intValue();
        Object result = orderService.getTopPurchasers(limit);
        return objectMapper.writeValueAsString(result);
    }
}
