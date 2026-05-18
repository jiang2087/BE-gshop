package com.example.demo.rag.tools.implementations;

import com.example.demo.rag.tools.ProductVariantTools;
import com.example.demo.rag.tools.spec.BaseTool;
import com.example.demo.rag.tools.spec.ToolHandler;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetProductByIdTool extends BaseTool implements ToolHandler {

    private final ProductVariantTools productVariantTools;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "get_product_by_id";
    }

    @Override
    public String getDescription() {
        return "Use when user asks detail of one specific product and has productId.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "productId", Map.of("type", "integer")
            ),
            "required", List.of("productId")
        );
    }

    @Override
    public String execute(Map<String, Object> arguments) throws Exception {
        Long productId = ((Number) arguments.get("productId")).longValue();
        Object result = productVariantTools.getProductById(productId);
        return objectMapper.writeValueAsString(result);
    }
}
