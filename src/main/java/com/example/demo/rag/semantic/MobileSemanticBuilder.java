package com.example.demo.rag.semantic;

import com.example.demo.dto.product.ProductDetailDto;
import org.springframework.stereotype.Component;

@Component
public class MobileSemanticBuilder implements ProductSemanticBuilder {

    @Override
    public boolean supports(String productType) {
        return "MOBILE".equalsIgnoreCase(productType);
    }

    @Override
    public String build(ProductDetailDto product) {
        StringBuilder sb = new StringBuilder();

        sb.append(product.name()).append(" mobile phone by ").append(product.brand()).append(".\n");

        var attrs = product.productAttributes();
        if (attrs != null) {
            appendIfPresent(sb, attrs.get("model"), attrs.get("model") + " model.\n");
            appendIfPresent(sb, attrs.get("screenSize"), attrs.get("screenSize") + " inch screen.\n");
            appendIfPresent(sb, attrs.get("resolution"), attrs.get("resolution") + " resolution.\n");
            appendIfPresent(sb, attrs.get("camera"), attrs.get("camera") + " camera.\n");
            appendIfPresent(sb, attrs.get("battery"), attrs.get("battery") + " battery.\n");
            appendIfPresent(sb, attrs.get("dimension"), attrs.get("dimension") + " dimension.\n");
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

        String camera = safe(attrs.get("camera")).toLowerCase();
        String battery = safe(attrs.get("battery")).toLowerCase();
        String resolution = safe(attrs.get("resolution")).toLowerCase();
        Object screenSizeObj = attrs.get("screenSize");

        if (camera.contains("108mp") || camera.contains("200mp") ||
            camera.contains("50mp") || camera.contains("64mp")) {
            useCases.add("high-quality photography");
            useCases.add("high-quality video recording");
            useCases.add("content creation");
        }

        if (battery.contains("5000") || battery.contains("6000") ||
            battery.contains("7000")) {
            useCases.add("heavy daily usage");
            useCases.add("long battery life");
            useCases.add("travel");
        }

        if (resolution.contains("2k") || resolution.contains("4k") ||
            resolution.contains("quad hd") || resolution.contains("qhd")) {
            useCases.add("high-quality streaming");
            useCases.add("gaming");
        }

        if (screenSizeObj != null) {
            try {
                double screenSize = Double.parseDouble(screenSizeObj.toString());
                if (screenSize >= 6.5) {
                    useCases.add("entertainment");
                    useCases.add("video watching");
                } else if (screenSize <= 6.0) {
                    useCases.add("compact one-hand use");
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (camera.contains("12mp") || camera.contains("16mp")) {
            useCases.add("daily photography");
        }

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

        String camera = safe(attrs.get("camera")).toLowerCase();
        String battery = safe(attrs.get("battery")).toLowerCase();
        String resolution = safe(attrs.get("resolution")).toLowerCase();
        Object screenSizeObj = attrs.get("screenSize");

        if ((camera.contains("108mp") || camera.contains("200mp") || camera.contains("64mp")) &&
            (resolution.contains("2k") || resolution.contains("4k"))) {
            categories.add("premium smartphone");
            categories.add("flagship phone");
        }
        else if ((camera.contains("48mp") || camera.contains("50mp")) &&
                 (battery.contains("4000") || battery.contains("5000"))) {
            categories.add("mid-range smartphone");
            categories.add("value phone");
        }
        else if (camera.contains("12mp") || camera.contains("13mp") || camera.contains("16mp")) {
            categories.add("entry-level smartphone");
            categories.add("budget phone");
        }
        else if (screenSizeObj != null) {
            try {
                double screenSize = Double.parseDouble(screenSizeObj.toString());
                if (screenSize >= 6.7) {
                    categories.add("large-screen smartphone");
                    categories.add("entertainment phone");
                } else {
                    categories.add("standard smartphone");
                }
            } catch (NumberFormatException ignored) {
                categories.add("standard smartphone");
            }
        }
        else {
            categories.add("standard smartphone");
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
