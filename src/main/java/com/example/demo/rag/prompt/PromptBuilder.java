package com.example.demo.rag.prompt;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PromptBuilder {

    private static final String TOOL_SELECTION_RULES = """
            [TOOL_SELECTION_RULES]
            - If question has specific productId or asks detail of one product, call tool get_product_by_id.
            - If question asks list/top/recommendation/many products, call tool get_products.
            - Do not call both tools unless user explicitly asks both list and detail.
            """;

    private static final String DEFAULT_TEMPLATE = """
            Ban la AI cua G-shop, ho tro khach hang chon san pham va dua ra goi y.
            Ban duoc phat trien boi Hau Giang.
            [TOOL_RULES]
            %s
            [RETRIEVED_CONTEXT]
            %s
            """;

    public static String buildSystemPrompt(RetrievalContext retrievalContext) {
        if (retrievalContext == null) {
            return DEFAULT_TEMPLATE.formatted(TOOL_SELECTION_RULES, "Khong co tai lieu lien quan.");
        }
        return DEFAULT_TEMPLATE.formatted(TOOL_SELECTION_RULES, retrievalContext.toContextText());
    }

    public static String buildSystemPrompt(RetrievalContext retrievalContext, String template) {
        String resolvedTemplate = (template == null || template.isBlank()) ? DEFAULT_TEMPLATE : template;
        String context = retrievalContext == null
                ? "Khong co tai lieu lien quan."
                : retrievalContext.toContextText();
        return resolvedTemplate.formatted(TOOL_SELECTION_RULES, context);
    }
}
