package com.example.demo.rag.client;

import com.example.demo.dto.message_ai.ChatCompletionResponse;
import com.example.demo.dto.message_ai.Choice;
import com.example.demo.dto.message_ai.ToolCall;
import com.example.demo.rag.ChatClientException;
import com.example.demo.rag.tools.DirectToolExecutor;
import com.example.demo.rag.tools.ToolExecutor;
import com.example.demo.rag.tools.ToolSelection;
import com.example.demo.rag.tools.ToolSelector;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class ToolCallOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(ToolCallOrchestrator.class);
    
    private final ToolExecutor toolExecutor;
    private final ChatApiClient chatApiClient;
    private final ChatClientConfig config;
    private final ToolSelector toolSelector;
    private final DirectToolExecutor directToolExecutor;
    private final ObjectMapper objectMapper;
    private static final String PRICE_RANGE_TOOL = "get_products_by_price_range";

    public ChatCompletionResponse executeWithTools(
            String model,
            List<Map<String, Object>> messages,
            List<Map<String, Object>> tools,
            String forcedToolName
    ) {
        String userQuery = extractLastUserMessage(messages);
        ToolSelection selection = toolSelector.selectTool(userQuery, tools);

        if (selection != null && selection.isShouldUseDirectExecution()) {
            return executeDirectFlow(model, messages, selection);
        }

        return executeLlmFlow(model, messages, tools, forcedToolName, userQuery);
    }

    private ChatCompletionResponse executeDirectFlow(
            String model,
            List<Map<String, Object>> messages,
            ToolSelection selection
    ) {
        logger.info("Using direct tool execution for: {}", selection.getToolName());
        
        String toolResult = directToolExecutor.executeDirectly(selection);
        if (toolResult == null) {
            logger.warn("Direct execution failed, falling back to LLM flow");
            return executeLlmFlow(model, messages, toolExecutor.buildToolSpecs(), null, extractLastUserMessage(messages));
        }

        List<Map<String, Object>> conversation = new ArrayList<>(messages);
        conversation.add(Map.of(
                "role", "system",
                "content", "Tool result: " + toolResult
        ));

        return chatApiClient.sendRequest(model, conversation, null, null, false);
    }

    private ChatCompletionResponse executeLlmFlow(
            String model,
            List<Map<String, Object>> messages,
            List<Map<String, Object>> tools,
            String forcedToolName,
            String userQuery
    ) {
        logger.info("Using LLM-based tool selection");
        
        List<Map<String, Object>> conversation = new ArrayList<>(messages);
        ChatCompletionResponse first = chatApiClient.sendRequest(
                model, conversation, tools, forcedToolName, false
        );

        List<ToolCall> toolCalls = extractToolCalls(first);
        if (toolCalls == null || toolCalls.isEmpty()) {
            return first;
        }

        conversation.add(Map.of(
                "role", "assistant",
                "tool_calls", toolCalls
        ));

        for (ToolCall toolCall : toolCalls) {
            validateToolCall(toolCall);
            String toolName = toolCall.getFunction().getName();
            String normalizedArgs = normalizeToolArguments(toolName, toolCall.getFunction().getArguments(), userQuery);
            String resultJson = toolExecutor.execute(
                    toolName,
                    normalizedArgs
            );

            conversation.add(Map.of(
                    "role", "tool",
                    "tool_call_id", toolCall.getId(),
                    "content", resultJson
            ));
        }

        return chatApiClient.sendRequest(model, conversation, null, null, false);
    }

    private String extractLastUserMessage(List<Map<String, Object>> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            Map<String, Object> msg = messages.get(i);
            if ("user".equals(msg.get("role"))) {
                Object content = msg.get("content");
                return content != null ? content.toString() : null;
            }
        }
        return null;
    }

    private List<ToolCall> extractToolCalls(ChatCompletionResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return null;
        }
        Choice firstChoice = response.getChoices().get(0);
        if (firstChoice == null || firstChoice.getMessage() == null) {
            return null;
        }
        return firstChoice.getMessage().getToolCalls();
    }

    private void validateToolCall(ToolCall toolCall) {
        if (toolCall == null || toolCall.getFunction() == null ||
                toolCall.getFunction().getName() == null ||
                toolCall.getFunction().getName().isBlank()) {
            throw new ChatClientException("Invalid tool call payload from model");
        }
    }

    private String normalizeToolArguments(String toolName, String rawArguments, String userQuery) {
        if (!PRICE_RANGE_TOOL.equals(toolName) || !isVndQuery(userQuery) || rawArguments == null || rawArguments.isBlank()) {
            return rawArguments;
        }
        try {
            Map<String, Object> args = objectMapper.readValue(rawArguments, new TypeReference<HashMap<String, Object>>() {});
            convertPriceField(args, "minPrice");
            convertPriceField(args, "maxPrice");
            return objectMapper.writeValueAsString(args);
        } catch (Exception ex) {
            logger.warn("Failed to normalize VND tool arguments. Fallback raw args.", ex);
            return rawArguments;
        }
    }

    private void convertPriceField(Map<String, Object> args, String fieldName) {
        Object value = args.get(fieldName);
        if (value == null) {
            return;
        }
        BigDecimal amount = parseVndAmount(value.toString());
        BigDecimal converted = amount.divide(config.getVndToUsdRate(), 2, RoundingMode.HALF_UP);
        args.put(fieldName, converted);
    }

    private BigDecimal parseVndAmount(String rawValue) {
        String normalized = rawValue.trim().toLowerCase()
                .replace("triệu", "tr")
                .replace("trieu", "tr");
        BigDecimal multiplier = BigDecimal.ONE;
        if (normalized.endsWith("k")) {
            multiplier = new BigDecimal("1000");
            normalized = normalized.substring(0, normalized.length() - 1);
        } else if (normalized.endsWith("tr")) {
            multiplier = new BigDecimal("1000000");
            normalized = normalized.substring(0, normalized.length() - 2);
        }
        String numeric = normalized.replaceAll("[,\\s]", "");
        return new BigDecimal(numeric).multiply(multiplier);
    }

    private boolean isVndQuery(String query) {
        if (query == null) {
            return false;
        }
        String normalized = query.toLowerCase();
        return normalized.contains("vnd") || normalized.contains("vnđ") || normalized.contains("dong") || normalized.contains("đ");
    }
}
