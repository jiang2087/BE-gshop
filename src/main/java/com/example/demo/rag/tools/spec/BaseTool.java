package com.example.demo.rag.tools.spec;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseTool implements Tool {
    
    @Override
    public Map<String, Object> toSpec() {
        Map<String, Object> function = new HashMap<>();
        function.put("name", getName());
        function.put("description", getDescription());
        function.put("parameters", getParametersSchema());
        
        Map<String, Object> spec = new HashMap<>();
        spec.put("type", "function");
        spec.put("function", function);
        
        return spec;
    }
}
