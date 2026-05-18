package com.example.demo.rag.tools;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DirectToolExecutor {
    private static final Logger logger = LoggerFactory.getLogger(DirectToolExecutor.class);
    
    private final ToolExecutor toolExecutor;
    private final ObjectMapper objectMapper;

    public String executeDirectly(ToolSelection selection) {
        if (selection == null || !selection.isShouldUseDirectExecution()) {
            return null;
        }

        try {
            logger.info("Executing tool directly: {} with confidence: {}",
                    selection.getToolName(), selection.getConfidence());

            String argsJson = objectMapper.writeValueAsString(selection.getArguments());
            return toolExecutor.execute(selection.getToolName(), argsJson);
        } catch (Exception e) {
            logger.error("Direct tool execution failed: {}", e.getMessage(), e);
            return null;
        }
    }
}
