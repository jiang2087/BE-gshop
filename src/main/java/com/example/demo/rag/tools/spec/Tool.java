package com.example.demo.rag.tools.spec;

import java.util.Map;

public interface Tool {
    String getName();
    String getDescription();
    Map<String, Object> getParametersSchema();
    Map<String, Object> toSpec();
    default ToolRole getRequiredRole() { return ToolRole.PUBLIC; }
}
