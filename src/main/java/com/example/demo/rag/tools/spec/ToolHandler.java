package com.example.demo.rag.tools.spec;

import java.util.Map;

public interface ToolHandler {
    String execute(Map<String, Object> arguments) throws Exception;
}
