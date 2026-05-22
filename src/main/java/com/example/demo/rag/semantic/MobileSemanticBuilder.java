package com.example.demo.rag.semantic;

import com.example.demo.dto.product.ProductDetailDto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class MobileSemanticBuilder extends AbstractSemanticBuilder {

    @Override
    public boolean supports(String productType) {
        return "MOBILE".equalsIgnoreCase(productType);
    }

    @Override
    public String build(ProductDetailDto product) {
        StringBuilder sb = new StringBuilder();

        sb.append(product.name()).append(" mobile phone by ").append(product.brand()).append(".\n");

        var attrs = product.productAttributes();
        if (hasAttributes(attrs)) {
            appendIfPresent(sb, attrs.get("model"), attrs.get("model") + " model.\n");
            appendIfPresent(sb, attrs.get("screenSize"), attrs.get("screenSize") + " inch screen.\n");
            appendIfPresent(sb, attrs.get("resolution"), attrs.get("resolution") + " resolution.\n");
            appendIfPresent(sb, attrs.get("camera"), attrs.get("camera") + " camera.\n");
            appendIfPresent(sb, attrs.get("battery"), attrs.get("battery") + " battery.\n");
            appendIfPresent(sb, attrs.get("dimension"), attrs.get("dimension") + " dimension.\n");
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

        String camera = safe(getAttribute(attrs, "camera")).toLowerCase();
        String battery = safe(getAttribute(attrs, "battery")).toLowerCase();
        String resolution = safe(getAttribute(attrs, "resolution")).toLowerCase();
        double screenSize = parseDouble(getAttribute(attrs, "screenSize"), 0);

        if (containsAny(camera, "108mp", "200mp", "50mp", "64mp")) {
            useCases.add("high-quality photography");
            useCases.add("high-quality video recording");
            useCases.add("content creation");
        }

        if (containsAny(battery, "5000", "6000", "7000")) {
            useCases.add("heavy daily usage");
            useCases.add("long battery life");
            useCases.add("travel");
        }

        if (containsAny(resolution, "2k", "4k", "quad hd", "qhd")) {
            useCases.add("high-quality streaming");
            useCases.add("gaming");
        }

        if (screenSize >= 6.5) {
            useCases.add("entertainment");
            useCases.add("video watching");
        } else if (screenSize > 0 && screenSize <= 6.0) {
            useCases.add("compact one-hand use");
        }

        if (containsAny(camera, "12mp", "16mp")) {
            useCases.add("daily photography");
        }

        return useCases;
    }

    private Set<String> buildCategories(Map<String, Object> attrs) {
        Set<String> categories = createOrderedSet();
        
        if (!hasAttributes(attrs)) {
            categories.add("standard smartphone");
            return categories;
        }

        String camera = safe(getAttribute(attrs, "camera")).toLowerCase();
        String battery = safe(getAttribute(attrs, "battery")).toLowerCase();
        String resolution = safe(getAttribute(attrs, "resolution")).toLowerCase();
        double screenSize = parseDouble(getAttribute(attrs, "screenSize"), 0);

        if (containsAny(camera, "108mp", "200mp", "64mp") && containsAny(resolution, "2k", "4k")) {
            categories.add("premium smartphone");
            categories.add("flagship phone");
        }
        else if (containsAny(camera, "48mp", "50mp") && containsAny(battery, "4000", "5000")) {
            categories.add("mid-range smartphone");
            categories.add("value phone");
        }
        else if (containsAny(camera, "12mp", "13mp", "16mp")) {
            categories.add("entry-level smartphone");
            categories.add("budget phone");
        }
        else if (screenSize >= 6.7) {
            categories.add("large-screen smartphone");
            categories.add("entertainment phone");
        }
        else {
            categories.add("standard smartphone");
        }

        return categories;
    }
}
