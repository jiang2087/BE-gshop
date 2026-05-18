package com.example.demo.rag.tools.implementations;

import com.example.demo.rag.tools.ProductVariantTools;
import com.example.demo.rag.tools.spec.BaseTool;
import com.example.demo.rag.tools.spec.ToolHandler;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetProductsTool extends BaseTool implements ToolHandler {

    private final ProductVariantTools productVariantTools;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "get_products";
    }

    @Override
    public String getDescription() {
        return "Use when user asks list, browse, top, or many products. Supports pagination and sorting by price or createdAt.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "page", Map.of("type", "integer"),
                "size", Map.of("type", "integer"),
                "sortBy", Map.of("type", "string", "enum", new String[]{"price", "createdAt"}),
                "sortDir", Map.of("type", "string", "enum", new String[]{"asc", "desc"})
            )
        );
    }

    @Override
    public String execute(Map<String, Object> arguments) throws Exception {
        Object result = getAllProducts(arguments);
        return objectMapper.writeValueAsString(result);
    }

    public Object getAllProducts(Map<String, Object> arguments) {
        Integer page = arguments.get("page") == null ? null : ((Number) arguments.get("page")).intValue();
        Integer size = arguments.get("size") == null ? null : ((Number) arguments.get("size")).intValue();
        String sortBy = arguments.get("sortBy") == null ? null : String.valueOf(arguments.get("sortBy"));
        String sortDir = arguments.get("sortDir") == null ? null : String.valueOf(arguments.get("sortDir"));
        return productVariantTools.getProducts(page, size, sortBy, sortDir);
    }
}
