package com.example.demo.rag.semantic;

import com.example.demo.dto.product.ProductDetailDto;
import org.springframework.stereotype.Component;

@Component
public class LaptopSemanticBuilder implements ProductSemanticBuilder {

    @Override
    public boolean supports(String productType) {
        return "LAPTOP".equalsIgnoreCase(productType);
    }

    @Override
    public String build(ProductDetailDto product) {
        StringBuilder sb = new StringBuilder();

        sb.append(product.name()).append(" laptop by ").append(product.brand()).append(".\n");

        var attrs = product.productAttributes();
        if (attrs != null) {
            appendIfPresent(sb, attrs.get("cpu"), attrs.get("cpu") + " processor.\n");
            appendIfPresent(sb, attrs.get("ram"), attrs.get("ram") + " RAM.\n");
            appendIfPresent(sb, attrs.get("gpu"), attrs.get("gpu") + " graphics.\n");
            appendIfPresent(sb, attrs.get("storage"), attrs.get("storage") + " storage.\n");
            appendIfPresent(sb, attrs.get("screenSize"), attrs.get("screenSize") + " inch display.\n");
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

        String gpu = safe(attrs.get("gpu")).toLowerCase();
        String ram = safe(attrs.get("ram")).toLowerCase();
        String cpu = safe(attrs.get("cpu")).toLowerCase();
        Object screenSizeObj = attrs.get("screenSize");

        if (gpu.contains("rtx") || gpu.contains("gtx")) {
            useCases.add("gaming");
            useCases.add("graphic design");
            useCases.add("video editing");
        }

        if (ram.contains("16") || ram.contains("32") || ram.contains("64")) {
            useCases.add("multitasking");
        }

        if (gpu.contains("iris") || gpu.contains("uhd") || gpu.contains("integrated")) {
            useCases.add("study");
            useCases.add("office work");
            useCases.add("web browsing");
        }

        if (screenSizeObj != null) {
            try {
                double screenSize = Double.parseDouble(screenSizeObj.toString());
                if (screenSize <= 14) {
                    useCases.add("frequent travel");
                    useCases.add("portable work");
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (cpu.contains("i3") || cpu.contains("i5") || cpu.contains("ryzen 3") || cpu.contains("ryzen 5")) {
            useCases.add("basic computing");
        }

        if (cpu.contains("i7") || cpu.contains("i9") || cpu.contains("ryzen 7") || cpu.contains("ryzen 9")) {
            useCases.add("professional work");
            useCases.add("content creation");
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
        String gpu = safe(attrs.get("gpu")).toLowerCase();
        String ram = safe(attrs.get("ram")).toLowerCase();
        String cpu = safe(attrs.get("cpu")).toLowerCase();

        if ((gpu.contains("rtx") || gpu.contains("gtx")) &&
            (ram.contains("16") || ram.contains("32") || ram.contains("64"))) {
            categories.add("gaming laptop");
            categories.add("high performance laptop");
        }
        else if ((cpu.contains("i5") || cpu.contains("i7") || cpu.contains("ryzen 5") || cpu.contains("ryzen 7")) &&
                 (ram.contains("8") || ram.contains("16"))) {
            categories.add("mid-range laptop");
            categories.add("office laptop");
            categories.add("study laptop");
        }
        else if (cpu.contains("i3") || cpu.contains("celeron") || cpu.contains("pentium") ||
                 cpu.contains("ryzen 3") || cpu.contains("athlon")) {
            categories.add("budget laptop");
            categories.add("entry-level laptop");
        }
        else if (cpu.contains("ultra") || cpu.contains("evo")) {
            categories.add("premium ultrabook");
            categories.add("portable laptop");
        }
        else {
            categories.add("general-purpose laptop");
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
