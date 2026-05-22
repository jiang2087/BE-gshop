package com.example.demo.rag.semantic;

import com.example.demo.dto.product.ProductDetailDto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class TelevisionSemanticBuilder extends AbstractSemanticBuilder {

    @Override
    public boolean supports(String productType) {
        return "TELEVISION".equalsIgnoreCase(productType);
    }

    @Override
    public String build(ProductDetailDto product) {
        StringBuilder sb = new StringBuilder();

        sb.append(product.name()).append(" television by ").append(product.brand()).append(".\n");

        var attrs = product.productAttributes();
        if (hasAttributes(attrs)) {
            appendIfPresent(sb, attrs.get("screenSize"), attrs.get("screenSize") + " inch screen.\n");
            appendIfPresent(sb, attrs.get("resolution"), attrs.get("resolution") + " resolution.\n");
            appendIfPresent(sb, attrs.get("refreshRate"), attrs.get("refreshRate") + "Hz refresh rate.\n");
            appendIfPresent(sb, attrs.get("weight"), attrs.get("weight") + " weight.\n");
            appendIfPresent(sb, attrs.get("warrantyMonths"), attrs.get("warrantyMonths") + " months warranty.\n");
        }

        Set<String> useCases = buildUseCases(attrs);
        appendUseCasesSection(sb, useCases);
        
        Set<String> categories = buildCategories(attrs);
        appendCategorySection(sb, categories);

        return sb.toString().trim();
    }

    private Set<String> buildUseCases(Map<String, Object> attrs) {
        Set<String> useCases = createOrderedSet();
        
        if (!hasAttributes(attrs)) {
            return useCases;
        }

        String resolution = safe(getAttribute(attrs, "resolution")).toLowerCase();
        int refreshRate = parseInt(getAttribute(attrs, "refreshRate"), 0);
        double screenSize = parseDouble(getAttribute(attrs, "screenSize"), 0);
        int warrantyMonths = parseInt(getAttribute(attrs, "warrantyMonths"), 0);

        if (containsAny(resolution, "4k", "uhd", "8k")) {
            useCases.add("high-quality movie watching");
            useCases.add("home entertainment");
            useCases.add("sports viewing");
        }

        if (refreshRate >= 120) {
            useCases.add("gaming");
            useCases.add("fast-action content");
        }

        if (screenSize >= 55) {
            useCases.add("large living room");
            useCases.add("group viewing");
        } else if (screenSize >= 32) {
            useCases.add("bedroom");
            useCases.add("small room");
        } else if (screenSize > 0) {
            useCases.add("small space");
        }

        if (containsAny(resolution, "full hd", "1080p")) {
            useCases.add("movies and TV shows");
        }

        if (resolution.contains("hd") && !containsAny(resolution, "full", "uhd", "4k")) {
            useCases.add("basic TV viewing");
        }

        if (warrantyMonths >= 24) {
            useCases.add("worry-free long-term use");
        }

        return useCases;
    }

    private Set<String> buildCategories(Map<String, Object> attrs) {
        Set<String> categories = createOrderedSet();
        
        if (!hasAttributes(attrs)) {
            categories.add("standard television");
            return categories;
        }

        String resolution = safe(getAttribute(attrs, "resolution")).toLowerCase();
        int refreshRate = parseInt(getAttribute(attrs, "refreshRate"), 0);
        double screenSize = parseDouble(getAttribute(attrs, "screenSize"), 0);

        if (containsAny(resolution, "8k", "4k") && refreshRate >= 120 && screenSize >= 55) {
            categories.add("premium television");
            categories.add("high-end TV");
        }
        else if (resolution.contains("4k") && screenSize >= 43) {
            categories.add("mid-range television");
            categories.add("4K TV");
        }
        else if (containsAny(resolution, "full hd", "1080p")) {
            categories.add("budget television");
            categories.add("Full HD TV");
        }
        else if (screenSize >= 65) {
            categories.add("large-screen television");
            categories.add("home entertainment TV");
        }
        else if (screenSize > 0 && screenSize <= 32) {
            categories.add("compact television");
            categories.add("small TV");
        }
        else {
            categories.add("standard television");
        }

        return categories;
    }
}
