package com.example.demo.rag.tools;

import com.example.demo.rag.tools.spec.Tool;
import com.example.demo.rag.tools.spec.ToolHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final List<Tool> tools;
    private final Map<String, ToolHandler> handlers = new HashMap<>();

    public void registerTool(Tool tool, ToolHandler handler) {
        handlers.put(normalizeToolName(tool.getName()), handler);
    }

    public List<Map<String, Object>> getAllToolSpecs() {
        return tools.stream()
                .map(Tool::toSpec)
                .collect(Collectors.toList());
    }

    public ToolHandler getHandler(String toolName) {
        String normalizedToolName = normalizeToolName(toolName);
        ToolHandler handler = handlers.get(normalizedToolName);
        if (handler == null) {
            handler = resolveHandlerFromTools(normalizedToolName);
            if (handler != null) {
                handlers.put(normalizedToolName, handler);
            }
        }
        if (handler == null) {
            throw new IllegalArgumentException("Unknown tool: " + normalizedToolName);
        }
        return handler;
    }

    public boolean hasHandler(String toolName) {
        return handlers.containsKey(normalizeToolName(toolName));
    }

    public Tool getTool(String toolName) {
        String normalizedToolName = normalizeToolName(toolName);
        return tools.stream()
                .filter(tool -> normalizeToolName(tool.getName()).equals(normalizedToolName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown tool: " + normalizedToolName));
    }

    private ToolHandler resolveHandlerFromTools(String normalizedToolName) {
        return tools.stream()
                .filter(tool -> normalizeToolName(tool.getName()).equals(normalizedToolName))
                .filter(ToolHandler.class::isInstance)
                .map(ToolHandler.class::cast)
                .findFirst()
                .orElse(null);
    }

    private String normalizeToolName(String toolName) {
        if (toolName == null) {
            return "";
        }
        return toolName.trim();
    }
}
