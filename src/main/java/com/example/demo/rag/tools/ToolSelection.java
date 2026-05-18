package com.example.demo.rag.tools;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ToolSelection {
    private String toolName;
    private Map<String, Object> arguments;
    private double confidence;
    private boolean shouldUseDirectExecution;
}
