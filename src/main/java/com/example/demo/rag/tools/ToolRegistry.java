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
        handlers.put(tool.getName(), handler);
    }

    public List<Map<String, Object>> getAllToolSpecs() {
        return tools.stream()
                .map(Tool::toSpec)
                .collect(Collectors.toList());
    }

    public ToolHandler getHandler(String toolName) {
        ToolHandler handler = handlers.get(toolName);
        if (handler == null) {
            throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
        return handler;
    }

    public boolean hasHandler(String toolName) {
        return handlers.containsKey(toolName);
    }

    public Tool getTool(String toolName) {
        return tools.stream()
                .filter(tool -> tool.getName().equals(toolName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown tool: " + toolName));
    }
}