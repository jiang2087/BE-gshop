package com.example.demo.rag.semantic;

import com.example.demo.dto.product.ProductDetailDto;
import org.springframework.stereotype.Component;

@Component
public class WatchesSemanticBuilder implements ProductSemanticBuilder {

    @Override
    public boolean supports(String productType) {
        return "WATCHES".equalsIgnoreCase(productType);
    }

    @Override
    public String build(ProductDetailDto product) {
        StringBuilder sb = new StringBuilder();

        sb.append(product.name()).append(" smartwatch by ").append(product.brand()).append(".\n");

        var attrs = product.productAttributes();
        if (attrs != null) {
            appendIfPresent(sb, attrs.get("model"), attrs.get("model") + " model.\n");
            appendIfPresent(sb, attrs.get("gender"), "Designed for " + attrs.get("gender") + ".\n");
            appendIfPresent(sb, attrs.get("screenSize"), attrs.get("screenSize") + " inch screen.\n");

            Object gpsObj = attrs.get("gps");
            if (gpsObj != null) {
                boolean hasGps = Boolean.parseBoolean(gpsObj.toString());
                sb.append(hasGps ? "With GPS.\n" : "Without GPS.\n");
            }

            appendIfPresent(sb, attrs.get("batteryLife"), attrs.get("batteryLife") + " days battery life.\n");
            appendIfPresent(sb, attrs.get("weight"), attrs.get("weight") + " weight.\n");
            appendIfPresent(sb, attrs.get("material"), attrs.get("material") + " material.\n");
        }

        appendUseCases(sb, attrs);
        appendCategory(sb, attrs);

        return sb.toString().trim();
    }

    private void appendUseCases(StringBuilder sb, java.util.Map<String, Object> attrs) {
        if (attrs == null) {
            return;
        }

        java.util.Set<String> useCases = new java.util.LinkedHashSet<>();

        String material = safe(attrs.get("material")).toLowerCase();
        String gender = safe(attrs.get("gender")).toLowerCase();
        Object gpsObj = attrs.get("gps");
        Object batteryLifeObj = attrs.get("batteryLife");
        Object screenSizeObj = attrs.get("screenSize");

        if (gpsObj != null && Boolean.parseBoolean(gpsObj.toString())) {
            useCases.add("running");
            useCases.add("hiking");
            useCases.add("outdoor activities");
        }

        if (batteryLifeObj != null) {
            try {
                double batteryLife = Double.parseDouble(batteryLifeObj.toString());
                if (batteryLife >= 7) {
                    useCases.add("long-term use");
                    useCases.add("travel");
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (material.contains("titanium") || material.contains("sapphire") ||
            material.contains("ceramic")) {
            useCases.add("premium style");
            useCases.add("high durability");
        }

        if (material.contains("stainless") || material.contains("steel")) {
            useCases.add("daily wear");
            useCases.add("sporty style");
        }

        if (screenSizeObj != null) {
            try {
                double screenSize = Double.parseDouble(screenSizeObj.toString());
                if (screenSize >= 1.7) {
                    useCases.add("easy readability");
                }
            } catch (NumberFormatException ignored) {
            }
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

        if (!useCases.isEmpty()) {
            sb.append("\nBest for:\n");
            for (String useCase : useCases) {
                sb.append("- ").append(useCase).append("\n");
            }
        }
    }

    private void appendCategory(StringBuilder sb, java.util.Map<String, Object> attrs) {
        if (attrs == null) {
            return;
        }

        java.util.Set<String> categories = new java.util.LinkedHashSet<>();

        String material = safe(attrs.get("material")).toLowerCase();
        Object gpsObj = attrs.get("gps");
        Object batteryLifeObj = attrs.get("batteryLife");

        boolean hasGps = gpsObj != null && Boolean.parseBoolean(gpsObj.toString());

        double batteryLife = 0;
        if (batteryLifeObj != null) {
            try {
                batteryLife = Double.parseDouble(batteryLifeObj.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        if ((material.contains("titanium") || material.contains("sapphire") || material.contains("ceramic")) &&
            hasGps && batteryLife >= 7) {
            categories.add("premium smartwatch");
            categories.add("luxury smartwatch");
        }
        else if (hasGps && batteryLife >= 5) {
            categories.add("sports smartwatch");
            categories.add("fitness watch");
        }
        else if (material.contains("stainless") || material.contains("aluminum")) {
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

        if (!categories.isEmpty()) {
            sb.append("\nCategory:\n");
            for (String category : categories) {
                sb.append(category).append("\n");
            }
        }
    }

    private void appendIfPresent(StringBuilder sb, Object value, String text) {
        if (value != null && !value.toString().trim().isEmpty()) {
            sb.append(text);
        }
    }

    private String safe(Object value) {
        return value != null ? value.toString() : "";
    }
}
