package com.example.demo.rag.semantic;

import com.example.demo.dto.product.ProductDetailDto;
import org.springframework.stereotype.Component;

@Component
public class TelevisionSemanticBuilder implements ProductSemanticBuilder {

    @Override
    public boolean supports(String productType) {
        return "TELEVISION".equalsIgnoreCase(productType);
    }

    @Override
    public String build(ProductDetailDto product) {
        StringBuilder sb = new StringBuilder();

        sb.append(product.name()).append(" television by ").append(product.brand()).append(".\n");

        var attrs = product.productAttributes();
        if (attrs != null) {
            appendIfPresent(sb, attrs.get("screenSize"), attrs.get("screenSize") + " inch screen.\n");
            appendIfPresent(sb, attrs.get("resolution"), attrs.get("resolution") + " resolution.\n");
            appendIfPresent(sb, attrs.get("refreshRate"), attrs.get("refreshRate") + "Hz refresh rate.\n");
            appendIfPresent(sb, attrs.get("weight"), attrs.get("weight") + " weight.\n");
            appendIfPresent(sb, attrs.get("warrantyMonths"), attrs.get("warrantyMonths") + " months warranty.\n");
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

        String resolution = safe(attrs.get("resolution")).toLowerCase();
        Object refreshRateObj = attrs.get("refreshRate");
        Object screenSizeObj = attrs.get("screenSize");
        Object warrantyMonthsObj = attrs.get("warrantyMonths");

        if (resolution.contains("4k") || resolution.contains("uhd") ||
            resolution.contains("8k")) {
            useCases.add("high-quality movie watching");
            useCases.add("home entertainment");
            useCases.add("sports viewing");
        }

        if (refreshRateObj != null) {
            try {
                int refreshRate = Integer.parseInt(refreshRateObj.toString());
                if (refreshRate >= 120) {
                    useCases.add("gaming");
                    useCases.add("fast-action content");
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (screenSizeObj != null) {
            try {
                double screenSize = Double.parseDouble(screenSizeObj.toString());
                if (screenSize >= 55) {
                    useCases.add("large living room");
                    useCases.add("group viewing");
                } else if (screenSize >= 32) {
                    useCases.add("bedroom");
                    useCases.add("small room");
                } else {
                    useCases.add("small space");
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (resolution.contains("full hd") || resolution.contains("1080p")) {
            useCases.add("movies and TV shows");
        }

        if (resolution.contains("hd") && !resolution.contains("full") &&
            !resolution.contains("uhd") && !resolution.contains("4k")) {
            useCases.add("basic TV viewing");
        }

        if (warrantyMonthsObj != null) {
            try {
                int warrantyMonths = Integer.parseInt(warrantyMonthsObj.toString());
                if (warrantyMonths >= 24) {
                    useCases.add("worry-free long-term use");
                }
            } catch (NumberFormatException ignored) {
            }
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

        String resolution = safe(attrs.get("resolution")).toLowerCase();
        Object refreshRateObj = attrs.get("refreshRate");
        Object screenSizeObj = attrs.get("screenSize");

        int refreshRate = 0;
        if (refreshRateObj != null) {
            try {
                refreshRate = Integer.parseInt(refreshRateObj.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        double screenSize = 0;
        if (screenSizeObj != null) {
            try {
                screenSize = Double.parseDouble(screenSizeObj.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        if ((resolution.contains("8k") || resolution.contains("4k")) &&
            refreshRate >= 120 && screenSize >= 55) {
            categories.add("premium television");
            categories.add("high-end TV");
        }
        else if (resolution.contains("4k") && screenSize >= 43) {
            categories.add("mid-range television");
            categories.add("4K TV");
        }
        else if (resolution.contains("full hd") || resolution.contains("1080p")) {
            categories.add("budget television");
            categories.add("Full HD TV");
        }
        else if (screenSize >= 65) {
            categories.add("large-screen television");
            categories.add("home entertainment TV");
        }
        else if (screenSize <= 32) {
            categories.add("compact television");
            categories.add("small TV");
        }
        else {
            categories.add("standard television");
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
