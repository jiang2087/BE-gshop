package com.example.demo.rag.semantic;

import com.example.demo.dto.product.ProductDetailDto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class WatchesSemanticBuilder extends AbstractSemanticBuilder {

    @Override
    public boolean supports(String productType) {
        return "WATCHES".equalsIgnoreCase(productType);
    }

    @Override
    public String build(ProductDetailDto product) {
        StringBuilder sb = new StringBuilder();

        sb.append(product.name()).append(" smartwatch by ").append(product.brand()).append(".\n");

        var attrs = product.productAttributes();
        if (hasAttributes(attrs)) {
            appendIfPresent(sb, attrs.get("model"), attrs.get("model") + " model.\n");
            appendIfPresent(sb, attrs.get("gender"), "Designed for " + attrs.get("gender") + ".\n");
            appendIfPresent(sb, attrs.get("screenSize"), attrs.get("screenSize") + " inch screen.\n");

            Object gpsObj = getAttribute(attrs, "gps");
            if (gpsObj != null) {
                boolean hasGps = parseBoolean(gpsObj, false);
                sb.append(hasGps ? "With GPS.\n" : "Without GPS.\n");
            }

            appendIfPresent(sb, attrs.get("batteryLife"), attrs.get("batteryLife") + " days battery life.\n");
            appendIfPresent(sb, attrs.get("weight"), attrs.get("weight") + " weight.\n");
            appendIfPresent(sb, attrs.get("material"), attrs.get("material") + " material.\n");
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

        String material = safe(getAttribute(attrs, "material")).toLowerCase();
        String gender = safe(getAttribute(attrs, "gender")).toLowerCase();
        boolean hasGps = parseBoolean(getAttribute(attrs, "gps"), false);
        double batteryLife = parseDouble(getAttribute(attrs, "batteryLife"), 0);
        double screenSize = parseDouble(getAttribute(attrs, "screenSize"), 0);

        if (hasGps) {
            useCases.add("running");
            useCases.add("hiking");
            useCases.add("outdoor activities");
        }

        if (batteryLife >= 7) {
            useCases.add("long-term use");
            useCases.add("travel");
        }

        if (containsAny(material, "titanium", "sapphire", "ceramic")) {
            useCases.add("premium style");
            useCases.add("high durability");
        }

        if (containsAny(material, "stainless", "steel")) {
            useCases.add("daily wear");
            useCases.add("sporty style");
        }

        if (screenSize >= 1.7) {
            useCases.add("easy readability");
        }

        if (!gender.isEmpty()) {
            if (gender.contains("male") && !gender.contains("female")) {
                useCases.add("men's fitness tracking");
            } else if (gender.contains("female")) {
                useCases.add("women's fitness tracking");
            } else if (gender.contains("unisex")) {
                useCases.add("unisex fitness tracking");
            }
        }

        useCases.add("health tracking");
        useCases.add("activity tracking");

        return useCases;
    }

    private Set<String> buildCategories(Map<String, Object> attrs) {
        Set<String> categories = createOrderedSet();
        
        if (!hasAttributes(attrs)) {
            categories.add("entry-level smartwatch");
            categories.add("basic smartwatch");
            return categories;
        }

        String material = safe(getAttribute(attrs, "material")).toLowerCase();
        boolean hasGps = parseBoolean(getAttribute(attrs, "gps"), false);
        double batteryLife = parseDouble(getAttribute(attrs, "batteryLife"), 0);

        if (containsAny(material, "titanium", "sapphire", "ceramic") && hasGps && batteryLife >= 7) {
            categories.add("premium smartwatch");
            categories.add("luxury smartwatch");
        }
        else if (hasGps && batteryLife >= 5) {
            categories.add("sports smartwatch");
            categories.add("fitness watch");
        }
        else if (containsAny(material, "stainless", "aluminum")) {
            categories.add("everyday smartwatch");
            categories.add("casual smartwatch");
        }
        else if (batteryLife >= 10) {
            categories.add("long-battery smartwatch");
            categories.add("extended battery watch");
        }
        else {
            categories.add("entry-level smartwatch");
            categories.add("basic smartwatch");
        }

        return categories;
    }
}
