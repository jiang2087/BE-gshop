package com.example.demo.rag.client;

import com.example.demo.dto.message_ai.ChatCompletionResponse;
import com.example.demo.rag.ChatClientException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ChatApiClient.class);
    
    private final ChatClientConfig config;
    private final RetryPolicy retryPolicy;
    private WebClient webClient;

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                    .baseUrl(config.getBaseUrl())
                    .defaultHeader("Content-Type", "application/json")
                    .build();
        }
        return webClient;
    }

    public ChatCompletionResponse sendRequest(
            String model,
            List<Map<String, Object>> messages,
            List<Map<String, Object>> tools,
            String forcedToolName,
            boolean stream
    ) {
        Map<String, Object> requestBody = buildRequestBody(model, messages, tools, forcedToolName, stream);
        
        try {
            return getWebClient()
                    .post()
                    .uri("/v1/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(ChatCompletionResponse.class)
                    .timeout(config.getRequestTimeout())
                    .retryWhen(retryPolicy.createRetrySpec())
                    .block();
        } catch (WebClientRequestException e) {
            logger.error("Network error calling chat API: {}", e.getMessage(), e);
            throw new ChatClientException("Network error: " + e.getMessage(), e);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error calling chat API: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ChatClientException(
                    "HTTP error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(),
                    e.getStatusCode().value()
            );
        } catch (Exception e) {
            logger.error("Unexpected error calling chat API: {}", e.getMessage(), e);
            throw new ChatClientException("Unexpected error: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildRequestBody(
            String model,
            List<Map<String, Object>> messages,
            List<Map<String, Object>> tools,
            String forcedToolName,
            boolean stream
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", stream);

        if (tools != null && !tools.isEmpty()) {
            body.put("tools", tools);
            if (forcedToolName != null && !forcedToolName.isBlank()) {
                body.put("tool_choice", Map.of(
                        "type", "function",
                        "function", Map.of("name", forcedToolName)
                ));
            } else {
                body.put("tool_choice", "auto");
            }
        }

        return body;
    }
}
