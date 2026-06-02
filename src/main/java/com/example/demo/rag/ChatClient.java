package com.example.demo.rag;

import com.example.demo.models.Product;
import com.example.demo.dto.response.RagChatResponse;
import com.example.demo.rag.client.RagOrchestrator;
import com.example.demo.services.ai.QdrantSearchService;
import com.example.demo.rag.memory.ChatMemoryService;
import com.example.demo.rag.memory.ChatTurn;
import com.example.demo.rag.prompt.QueryAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatClient {

    private final RagOrchestrator ragOrchestrator;
    private final ChatMemoryService chatMemoryService;
    private final QueryAnalyzer queryAnalyzer;

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
