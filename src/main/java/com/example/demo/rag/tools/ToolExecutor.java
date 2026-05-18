package com.example.demo.rag.tools;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.example.demo.rag.tools.spec.Tool;
import com.example.demo.rag.tools.spec.ToolRole;
import com.example.demo.rag.tools.exception.ToolAuthorizationException;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ToolExecutor {

    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    public List<Map<String, Object>> buildToolSpecs() {
        return toolRegistry.getAllToolSpecs();
    }

    public String execute(String functionName, String argumentsJson) {
        try {
            Map<String, Object> args = objectMapper.readValue(
                    argumentsJson == null ? "{}" : argumentsJson,
                    new TypeReference<>() {}
            );

            return toolRegistry.getHandler(functionName).execute(args);
        } catch (Exception e) {
            throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
        }
    }

    public String executeWithRole(String functionName, String argumentsJson, String userRole) {
        try {
            Tool tool = toolRegistry.getTool(functionName);
            ToolRole requiredRole = tool.getRequiredRole();
            
            if (!hasPermission(userRole, requiredRole)) {
                throw new ToolAuthorizationException(
                    "Access denied. Tool '" + functionName + "' requires " + requiredRole + " role."
                );
            }
            
            Map<String, Object> args = objectMapper.readValue(
                    argumentsJson == null ? "{}" : argumentsJson,
                    new TypeReference<>() {}
            );

            return toolRegistry.getHandler(functionName).execute(args);
        } catch (ToolAuthorizationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
        }
    }
    
    private boolean hasPermission(String userRole, ToolRole requiredRole) {
        if (requiredRole == ToolRole.PUBLIC) return true;
        if (userRole == null) return false;
        
        return switch (requiredRole) {
            case USER -> "USER".equals(userRole) || "ADMIN".equals(userRole);
            case ADMIN -> "ADMIN".equals(userRole);
            default -> true;
        };
    }
}