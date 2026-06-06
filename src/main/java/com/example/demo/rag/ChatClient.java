package com.example.demo.rag;

import com.example.demo.dto.response.RagChatResponse;
import com.example.demo.rag.client.ChatClientConfig;
import com.example.demo.rag.client.RagOrchestrator;
import com.example.demo.rag.client.ProductQueryDetector;
import com.example.demo.rag.memory.ChatMemoryConfig;
import com.example.demo.rag.memory.ChatMemoryService;
import com.example.demo.rag.memory.ChatTurn;
import com.example.demo.rag.prompt.QueryAnalyzer;
import com.example.demo.rag.prompt.PromptBuilder;
import com.example.demo.rag.prompt.RetrievalContext;
import com.example.demo.rag.retrieval.DenseRetrievalService;
import com.example.demo.rag.retrieval.SearchRequest;
import com.example.demo.rag.retrieval.SearchResult;
import com.example.demo.rag.tools.ToolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatClient {
    private static final String PRODUCT_IDS_MARKER = "[PRODUCT_IDS:";

    private final RagOrchestrator ragOrchestrator;
    private final ChatMemoryService chatMemoryService;
    private final QueryAnalyzer queryAnalyzer;
    private final DenseRetrievalService denseRetrievalService;
    private final ProductQueryDetector productQueryDetector;
    private final ChatClientConfig config;
    private final ChatMemoryConfig memoryConfig;
    private final ObjectMapper objectMapper;
    private final ToolExecutor toolExecutor;

    private WebClient streamWebClient;

    private record StreamToolCallDelta(int index, String id, String type, String name, String arguments) {}
    private record StreamEvent(String content, List<StreamToolCallDelta> toolCalls) {}
    private record BufferedToolCall(String id, String type, String name, String arguments) {}

    private static class ToolCallAccumulator {
        private String id;
        private String type;
        private String name;
        private final StringBuilder arguments = new StringBuilder();

        private void append(StreamToolCallDelta delta) {
            if (delta.id() != null && !delta.id().isBlank()) {
                id = delta.id();
            }
            if (delta.type() != null && !delta.type().isBlank()) {
                type = delta.type();
            }
            if (delta.name() != null && !delta.name().isBlank()) {
                name = delta.name();
            }
            if (delta.arguments() != null) {
                arguments.append(delta.arguments());
            }
        }

        private BufferedToolCall toBufferedToolCall() {
            return new BufferedToolCall(id, type, name, arguments.toString());
        }
    }

    public RagChatResponse callChatWithRag(String userQuery, int retrievalLimit) {
        int dynamicLimit = queryAnalyzer.extractProductLimit(userQuery, retrievalLimit);
        return ragOrchestrator.executeRagFlow(userQuery, dynamicLimit);
    }

    public RagChatResponse callChatWithRag(String conversationId, String userQuery, int retrievalLimit) {
        if (userQuery == null || userQuery.isBlank()) {
            throw new IllegalArgumentException("userQuery must not be blank");
        }
        String effectiveConversationId = (conversationId == null || conversationId.isBlank())
                ? createConversationId()
                : conversationId;

        List<ChatTurn> history = chatMemoryService.getConversation(effectiveConversationId);

        int dynamicLimit = queryAnalyzer.extractProductLimit(userQuery, retrievalLimit);
        
        // Keep current user message clean for tool selection; pass history separately.
        RagChatResponse response = ragOrchestrator.executeRagFlow(userQuery, dynamicLimit, history);
        
        String assistantAnswer = ensureAnswerText(response.answer());
        
        assistantAnswer = formatCurrencyWithVnd(assistantAnswer);

        List<Long> llmSelectedProductIds = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[PRODUCT_IDS:\\s*(.*?)\\]");
        java.util.regex.Matcher matcher = pattern.matcher(assistantAnswer);
        if (matcher.find()) {
            String idsStr = matcher.group(1);
            String[] ids = idsStr.split(",");
            for (String idStr : ids) {
                try {
                    llmSelectedProductIds.add(Long.parseLong(idStr.trim()));
                } catch (NumberFormatException e) {
                    // Ignore invalid IDs
                }
            }
            assistantAnswer = matcher.replaceFirst("").trim();
        }

        chatMemoryService.appendUserMessage(effectiveConversationId, userQuery);
        chatMemoryService.appendAssistantMessage(effectiveConversationId, assistantAnswer, null);

        // Filter products based on LLM's selection or fallback to RAG retrieved products
        List<Long> mergedProducts = new ArrayList<Long>();
        if (!llmSelectedProductIds.isEmpty()) {
            mergedProducts.addAll(llmSelectedProductIds);
        } else if (response.products() != null) {
            mergedProducts.addAll(response.products());
        }

        return new RagChatResponse(effectiveConversationId, assistantAnswer, mergedProducts, null);
    }

    public Flux<String> callChatWithRagStream(String userQuery, int retrievalLimit) {
        return callChatWithRagStream(null, userQuery, retrievalLimit);
    }

    public Flux<String> callChatWithRagStream(String conversationId, String userQuery, int retrievalLimit) {
        if (userQuery == null || userQuery.isBlank()) {
            return Flux.error(new IllegalArgumentException("userQuery must not be blank"));
        }

        String effectiveConversationId = (conversationId == null || conversationId.isBlank())
                ? createConversationId()
                : conversationId;
        List<ChatTurn> history = chatMemoryService.getConversation(effectiveConversationId);
        int dynamicLimit = queryAnalyzer.extractProductLimit(userQuery, retrievalLimit);
        List<SearchResult> retrieved = retrieveContext(userQuery, dynamicLimit);
        List<Map<String, Object>> messages = buildRagMessages(userQuery, retrieved, history);
        List<Long> retrievedProductIds = extractProducts(userQuery, retrieved);

        StringBuilder fullAnswer = new StringBuilder();

        return streamChatCompletionWithTools(messages)
                .doOnNext(fullAnswer::append)
                .transform(this::hideProductIdsMarker)
                .doOnComplete(() -> saveStreamedConversation(
                        effectiveConversationId,
                        userQuery,
                        fullAnswer.toString(),
                        retrievedProductIds
                ));
    }

    public void clearConversation(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId must not be blank");
        }
        chatMemoryService.clearConversation(conversationId);
    }

    public void clearAllConversations() {
        chatMemoryService.clearAllConversations();
    }

    public String createConversationId() {
        return UUID.randomUUID().toString();
    }

    public List<ChatTurn> getConversation(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId must not be blank");
        }
        return chatMemoryService.getConversation(conversationId);
    }

    private List<SearchResult> retrieveContext(String userQuery, int retrievalLimit) {
        return denseRetrievalService.search(
                SearchRequest.builder()
                        .query(userQuery)
                        .limit(retrievalLimit)
                        .build()
        ).getResults();
    }

    private List<Map<String, Object>> buildRagMessages(
            String userQuery,
            List<SearchResult> retrieved,
            List<ChatTurn> conversationHistory
    ) {
        RetrievalContext retrievalContext = RetrievalContext.builder()
                .query(userQuery)
                .results(retrieved)
                .build();

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", PromptBuilder.buildSystemPrompt(retrievalContext)));

        for (ChatTurn turn : trimHistoryForPrompt(conversationHistory)) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("role", turn.role());
            msg.put("content", turn.content());
            messages.add(msg);
        }

        messages.add(Map.of("role", "user", "content", userQuery));
        return messages;
    }

    private Flux<String> streamChatCompletion(List<Map<String, Object>> messages) {
        return streamChatCompletionEvents(messages, null, null)
                .map(StreamEvent::content)
                .filter(chunk -> chunk != null && !chunk.isEmpty());
    }

    private Flux<String> streamChatCompletionWithTools(List<Map<String, Object>> messages) {
        List<Map<String, Object>> tools = toolExecutor.buildToolSpecs();
        if (tools == null || tools.isEmpty()) {
            return streamChatCompletion(messages);
        }

        Map<Integer, ToolCallAccumulator> toolCallAccumulators = new HashMap<>();
        StringBuilder firstAnswer = new StringBuilder();

        return streamChatCompletionEvents(messages, tools, null)
                .doOnNext(event -> {
                    appendToolCallDeltas(toolCallAccumulators, event.toolCalls());
                    if (event.content() != null && !event.content().isEmpty()) {
                        firstAnswer.append(event.content());
                    }
                })
                .ignoreElements()
                .thenMany(Flux.defer(() -> {
                    List<BufferedToolCall> toolCalls = bufferedToolCalls(toolCallAccumulators);
                    if (toolCalls.isEmpty()) {
                        return firstAnswer.isEmpty() ? Flux.empty() : Flux.just(firstAnswer.toString());
                    }
                    return streamFinalAnswerAfterTools(messages, toolCalls);
                }));
    }

    private Flux<String> streamFinalAnswerAfterTools(
            List<Map<String, Object>> messages,
            List<BufferedToolCall> toolCalls
    ) {
        List<Map<String, Object>> conversation = new ArrayList<>(messages);
        conversation.add(buildAssistantToolCallMessage(toolCalls));

        for (BufferedToolCall toolCall : toolCalls) {
            String resultJson = toolExecutor.execute(toolCall.name(), toolCall.arguments());
            conversation.add(Map.of(
                    "role", "tool",
                    "tool_call_id", toolCall.id(),
                    "content", resultJson
            ));
        }

        return streamChatCompletion(conversation);
    }

    private Map<String, Object> buildAssistantToolCallMessage(List<BufferedToolCall> toolCalls) {
        List<Map<String, Object>> toolCallMessages = new ArrayList<>();
        for (BufferedToolCall toolCall : toolCalls) {
            toolCallMessages.add(Map.of(
                    "id", toolCall.id(),
                    "type", toolCall.type() == null || toolCall.type().isBlank() ? "function" : toolCall.type(),
                    "function", Map.of(
                            "name", toolCall.name(),
                            "arguments", toolCall.arguments() == null ? "{}" : toolCall.arguments()
                    )
            ));
        }

        Map<String, Object> assistantMsg = new HashMap<>();
        assistantMsg.put("role", "assistant");
        assistantMsg.put("tool_calls", toolCallMessages);
        return assistantMsg;
    }

    private void appendToolCallDeltas(
            Map<Integer, ToolCallAccumulator> toolCallAccumulators,
            List<StreamToolCallDelta> deltas
    ) {
        if (deltas == null || deltas.isEmpty()) {
            return;
        }
        for (StreamToolCallDelta delta : deltas) {
            ToolCallAccumulator accumulator = toolCallAccumulators.computeIfAbsent(
                    delta.index(),
                    ignored -> new ToolCallAccumulator()
            );
            accumulator.append(delta);
        }
    }

    private List<BufferedToolCall> bufferedToolCalls(Map<Integer, ToolCallAccumulator> toolCallAccumulators) {
        return toolCallAccumulators.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().toBufferedToolCall())
                .filter(toolCall -> toolCall.name() != null && !toolCall.name().isBlank())
                .toList();
    }

    private Flux<StreamEvent> streamChatCompletionEvents(
            List<Map<String, Object>> messages,
            List<Map<String, Object>> tools,
            String forcedToolName
    ) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getDefaultModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", true);

        if (tools != null && !tools.isEmpty()) {
            requestBody.put("tools", tools);
            if (forcedToolName != null && !forcedToolName.isBlank()) {
                requestBody.put("tool_choice", Map.of(
                        "type", "function",
                        "function", Map.of("name", forcedToolName)
                ));
            } else {
                requestBody.put("tool_choice", "auto");
            }
        }

        return getStreamWebClient()
                .post()
                .uri("/v1/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(config.getRequestTimeout())
                .flatMapIterable(this::extractStreamPayloads)
                .takeUntil("[DONE]"::equals)
                .filter(payload -> !"[DONE]".equals(payload))
                .map(this::extractStreamEvent)
                .filter(event -> (event.content() != null && !event.content().isEmpty())
                        || (event.toolCalls() != null && !event.toolCalls().isEmpty()))
                .onErrorMap(WebClientRequestException.class,
                        e -> new ChatClientException("Network error: " + e.getMessage(), e))
                .onErrorMap(WebClientResponseException.class,
                        e -> new ChatClientException("HTTP error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e.getStatusCode().value()));
    }

    private WebClient getStreamWebClient() {
        if (streamWebClient == null) {
            streamWebClient = WebClient.builder()
                    .baseUrl(config.getBaseUrl())
                    .defaultHeader("Content-Type", "application/json")
                    .build();
        }
        return streamWebClient;
    }

    private List<String> extractStreamPayloads(String rawChunk) {
        if (rawChunk == null || rawChunk.isBlank()) {
            return List.of();
        }

        List<String> payloads = new ArrayList<>();
        for (String line : rawChunk.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("data:")) {
                payloads.add(trimmed.substring(5).trim());
            } else if (trimmed.startsWith("{") || "[DONE]".equals(trimmed)) {
                payloads.add(trimmed);
            }
        }
        return payloads;
    }

    private StreamEvent extractStreamEvent(String payload) {
        try {
            Map<String, Object> root = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
            Object choicesObj = root.get("choices");
            if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
                return new StreamEvent("", List.of());
            }
            Object firstChoice = choices.getFirst();
            if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
                return new StreamEvent("", List.of());
            }
            Object deltaObj = choiceMap.get("delta");
            if (!(deltaObj instanceof Map<?, ?> deltaMap)) {
                return new StreamEvent("", List.of());
            }
            Object content = deltaMap.get("content");
            return new StreamEvent(
                    content == null ? "" : content.toString(),
                    extractToolCallDeltas(deltaMap)
            );
        } catch (Exception e) {
            log.debug("Failed to parse streaming payload: {}", payload, e);
            return new StreamEvent("", List.of());
        }
    }

    private List<StreamToolCallDelta> extractToolCallDeltas(Map<?, ?> deltaMap) {
        Object toolCallsObj = deltaMap.get("tool_calls");
        if (!(toolCallsObj instanceof List<?> toolCalls) || toolCalls.isEmpty()) {
            return List.of();
        }

        List<StreamToolCallDelta> deltas = new ArrayList<>();
        for (Object toolCallObj : toolCalls) {
            if (!(toolCallObj instanceof Map<?, ?> toolCallMap)) {
                continue;
            }

            int index = parseToolCallIndex(toolCallMap.get("index"));
            String id = toNullableString(toolCallMap.get("id"));
            String type = toNullableString(toolCallMap.get("type"));
            String name = null;
            String arguments = null;

            Object functionObj = toolCallMap.get("function");
            if (functionObj instanceof Map<?, ?> functionMap) {
                name = toNullableString(functionMap.get("name"));
                arguments = toNullableString(functionMap.get("arguments"));
            }

            deltas.add(new StreamToolCallDelta(index, id, type, name, arguments));
        }
        return deltas;
    }

    private int parseToolCallIndex(Object rawIndex) {
        if (rawIndex instanceof Number number) {
            return number.intValue();
        }
        if (rawIndex != null) {
            try {
                return Integer.parseInt(rawIndex.toString());
            } catch (NumberFormatException e) {
                log.debug("Invalid streamed tool call index: {}", rawIndex);
            }
        }
        return 0;
    }

    private String toNullableString(Object value) {
        return value == null ? null : value.toString();
    }

    private Flux<String> hideProductIdsMarker(Flux<String> chunks) {
        AtomicReference<String> pending = new AtomicReference<>("");
        AtomicBoolean suppressRest = new AtomicBoolean(false);

        return chunks.<String>handle((chunk, sink) -> {
            if (suppressRest.get()) {
                return;
            }

            String buffered = pending.get() + chunk;
            int markerIndex = buffered.indexOf(PRODUCT_IDS_MARKER);
            if (markerIndex >= 0) {
                String visible = buffered.substring(0, markerIndex).trim();
                if (!visible.isEmpty()) {
                    sink.next(visible);
                }
                pending.set("");
                suppressRest.set(true);
                return;
            }

            int safeLength = Math.max(0, buffered.length() - PRODUCT_IDS_MARKER.length());
            if (safeLength > 0) {
                sink.next(buffered.substring(0, safeLength));
                pending.set(buffered.substring(safeLength));
            } else {
                pending.set(buffered);
            }
        }).concatWith(Flux.defer(() -> {
            if (suppressRest.get() || pending.get().isEmpty()) {
                return Flux.empty();
            }
            return Flux.just(pending.get());
        }));
    }

    private void saveStreamedConversation(
            String conversationId,
            String userQuery,
            String rawAnswer,
            List<Long> retrievedProductIds
    ) {
        String assistantAnswer = ensureAnswerText(rawAnswer);
        assistantAnswer = formatCurrencyWithVnd(assistantAnswer);

        List<Long> llmSelectedProductIds = new ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[PRODUCT_IDS:\\s*(.*?)\\]");
        java.util.regex.Matcher matcher = pattern.matcher(assistantAnswer);
        if (matcher.find()) {
            String[] ids = matcher.group(1).split(",");
            for (String idStr : ids) {
                try {
                    llmSelectedProductIds.add(Long.parseLong(idStr.trim()));
                } catch (NumberFormatException e) {
                    log.debug("Ignore invalid streamed product id: {}", idStr);
                }
            }
            assistantAnswer = matcher.replaceFirst("").trim();
        }

        List<Long> mergedProducts = new ArrayList<>();
        if (!llmSelectedProductIds.isEmpty()) {
            mergedProducts.addAll(llmSelectedProductIds);
        } else if (retrievedProductIds != null) {
            mergedProducts.addAll(retrievedProductIds);
        }

        chatMemoryService.appendUserMessage(conversationId, userQuery);
        chatMemoryService.appendAssistantMessage(conversationId, assistantAnswer, null);
    }

    private List<ChatTurn> trimHistoryForPrompt(List<ChatTurn> conversationHistory) {
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            return List.of();
        }

        int maxTurns = Math.max(1, memoryConfig.getMaxPromptTurns());
        int maxChars = Math.max(500, memoryConfig.getMaxPromptChars());
        List<ChatTurn> selected = new ArrayList<>();
        int usedChars = 0;

        for (int i = conversationHistory.size() - 1; i >= 0 && selected.size() < maxTurns; i--) {
            ChatTurn turn = conversationHistory.get(i);
            if (turn == null || turn.content() == null || turn.content().isBlank()) {
                continue;
            }
            ChatTurn promptTurn = truncateTurn(turn, Math.max(1, maxChars - usedChars));
            int contentChars = promptTurn.content().length();
            if (!selected.isEmpty() && usedChars + contentChars > maxChars) {
                break;
            }
            selected.add(promptTurn);
            usedChars += contentChars;
        }

        java.util.Collections.reverse(selected);
        return selected;
    }

    private ChatTurn truncateTurn(ChatTurn turn, int maxChars) {
        if (turn.content().length() <= maxChars) {
            return turn;
        }
        String content = turn.content().substring(0, Math.max(0, maxChars - 3)) + "...";
        return new ChatTurn(turn.role(), content, null, turn.timestamp());
    }

    private List<Long> extractProducts(String userQuery, List<SearchResult> retrieved) {
        if (!productQueryDetector.isProductRelatedQuery(userQuery)) {
            return List.of();
        }

        Set<Long> productIds = new LinkedHashSet<>();
        for (SearchResult result : retrieved) {
            if (result != null && result.getProductId() != null) {
                productIds.add(result.getProductId());
            }
        }

        return productIds.stream().toList();
    }

    private String ensureAnswerText(String answer) {
        if (answer == null || answer.isBlank()) {
            return "Mình chưa có đủ ngữ cảnh để trả lời chính xác. Bạn có thể cung cấp thêm chi tiết (tên sản phẩm, mức giá, hoặc nhu cầu cụ thể) để mình hỗ trợ tốt hơn.";
        }
        return answer;
    }

    private String formatCurrencyWithVnd(String text) {
        if (text == null) return null;
        
        String numRegex = "\\d+(?:[.,]\\d+)*";
        String rangeRegex1 = "\\$?\\s*" + numRegex + "\\s*(?:-|đến|to)\\s*\\$?\\s*" + numRegex + "\\s*\\$";
        String rangeRegex2 = "\\$\\s*" + numRegex + "\\s*(?:-|đến|to)\\s*\\$?\\s*" + numRegex;
        String singleRegex1 = numRegex + "\\s*\\$";
        String singleRegex2 = "\\$\\s*" + numRegex;
        
        String combinedRegex = "(?i)(?:" + rangeRegex1 + "|" + rangeRegex2 + "|" + singleRegex1 + "|" + singleRegex2 + ")";
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(combinedRegex);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        java.util.regex.Pattern numPattern = java.util.regex.Pattern.compile(numRegex);
        
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String matchStr = matcher.group(0);
            java.util.regex.Matcher numMatcher = numPattern.matcher(matchStr);
            java.util.List<Double> nums = new java.util.ArrayList<>();
            while (numMatcher.find()) {
                String nStr = numMatcher.group().replace(",", "");
                try {
                    nums.add(Double.parseDouble(nStr));
                } catch (NumberFormatException e) {
                    log.error(e.getMessage());
                }
            }
            
            double rate = 25.4; // 25.4k VND per USD
            if (nums.isEmpty()) {
                matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(matchStr));
            } else if (nums.size() == 1) {
                String vndStr = formatVndAmount(nums.get(0) * rate);
                matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(matchStr + "(~ " + vndStr + " VND)"));
            } else {
                String vndStr1 = formatVndAmount(nums.get(0) * rate);
                String vndStr2 = formatVndAmount(nums.get(1) * rate);
                matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(matchStr + "(~ " + vndStr1 + " - " + vndStr2 + " VND)"));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String formatVndAmount(double amountInK) {
        if (amountInK >= 1000) {
            double tr = amountInK / 1000.0;
            String str = String.format(java.util.Locale.US, "%.3f", tr);
            while (str.contains(".") && (str.endsWith("0") || str.endsWith("."))) {
                str = str.substring(0, str.length() - 1);
            }
            return str.replace(".", ",") + " tr";
        } else {
            if (amountInK == (long) amountInK) {
                return String.format("%d", (long) amountInK) + "k";
            } else {
                String str = String.format(java.util.Locale.US, "%.1f", amountInK);
                if (str.endsWith(".0")) {
                    str = str.substring(0, str.length() - 2);
                }
                return str.replace(".", ",") + "k";
            }
        }
    }

    private void validateInput(String model, List<Map<String, Object>> messages) {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("model must not be blank");
        }
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages must not be empty");
        }
    }
}
