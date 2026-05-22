package com.example.demo.rag.semantic;

import com.example.demo.dto.product.ProductDetailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class LaptopSemanticBuilder extends AbstractSemanticBuilder {

    @Override
    public boolean supports(String productType) {
        return "LAPTOP".equalsIgnoreCase(productType);
    }

    @Override
    public String build(ProductDetailDto product) {
        log.debug("Building natural-language text for laptop: {}", product.name());
        StringBuilder sb = new StringBuilder();

        sb.append(product.name()).append(" laptop by ").append(product.brand()).append(".\n");

        var attrs = product.productAttributes();
        if (hasAttributes(attrs)) {
            log.debug("Product {} has {} attributes: {}", product.id(), attrs.size(), attrs.keySet());
            appendIfPresent(sb, attrs.get("cpu"), attrs.get("cpu") + " processor.\n");
            appendIfPresent(sb, attrs.get("ram"), attrs.get("ram") + " RAM.\n");
            appendIfPresent(sb, attrs.get("gpu"), attrs.get("gpu") + " graphics.\n");
            appendIfPresent(sb, attrs.get("storage"), attrs.get("storage") + " storage.\n");
            
            Object screenSizeObj = attrs.get("screenSize");
            if (screenSizeObj != null && !screenSizeObj.toString().trim().isEmpty()) {
                String normalized = normalizeScreenSize(screenSizeObj);
                sb.append(normalized).append(" inch display.\n");
            }
        } else {
            log.warn("Product {} has null attributes", product.id());
        }

        Set<String> useCases = buildUseCases(attrs);
        appendUseCasesSection(sb, useCases);
        
        Set<String> categories = buildCategories(attrs);
        appendCategorySection(sb, categories);

        String result = sb.toString().trim();
        log.debug("Generated natural-language text length: {} chars", result.length());
        return result;
    }

    private Set<String> buildUseCases(Map<String, Object> attrs) {
        Set<String> useCases = createOrderedSet();
        
        if (!hasAttributes(attrs)) {
            log.debug("Skipping use cases - attrs is null");
            return useCases;
        }

        String gpu = safe(getAttribute(attrs, "gpu")).toLowerCase();
        String ram = safe(getAttribute(attrs, "ram")).toLowerCase();
        String cpu = safe(getAttribute(attrs, "cpu")).toLowerCase();
        double screenSize = parseDouble(getAttribute(attrs, "screenSize"), 0);

        log.debug("Analyzing use cases - gpu: '{}', ram: '{}', cpu: '{}', screenSize: '{}'", 
                gpu, ram, cpu, screenSize);

        if (containsAny(gpu, "rtx", "gtx")) {
            useCases.add("gaming");
            useCases.add("graphic design");
            useCases.add("video editing");
        }

        if (containsAny(ram, "16", "32", "64")) {
            useCases.add("multitasking");
        }

        if (containsAny(gpu, "iris", "uhd", "integrated")) {
            useCases.add("study");
            useCases.add("office work");
            useCases.add("web browsing");
        }

        if (screenSize > 0 && screenSize <= 14) {
            useCases.add("frequent travel");
            useCases.add("portable work");
        }

        if (containsAny(cpu, "i3", "i5", "ryzen 3", "ryzen 5")) {
            useCases.add("basic computing");
        }

        if (containsAny(cpu, "i7", "i9", "ryzen 7", "ryzen 9")) {
            useCases.add("professional work");
            useCases.add("content creation");
        }

        log.debug("Found {} use cases: {}", useCases.size(), useCases);
        if (useCases.isEmpty()) {
            log.warn("No use cases identified for this laptop");
        }

        return useCases;
    }

    private Set<String> buildCategories(Map<String, Object> attrs) {
        Set<String> categories = createOrderedSet();
        
        if (!hasAttributes(attrs)) {
            log.debug("Skipping category - attrs is null");
            return categories;
        }

        String gpu = safe(getAttribute(attrs, "gpu")).toLowerCase();
        String ram = safe(getAttribute(attrs, "ram")).toLowerCase();
        String cpu = safe(getAttribute(attrs, "cpu")).toLowerCase();

        if (containsAny(gpu, "rtx", "gtx") && containsAny(ram, "16", "32", "64")) {
            categories.add("gaming laptop");
            categories.add("high performance laptop");
        }
        else if (containsAny(cpu, "i5", "i7", "ryzen 5", "ryzen 7") && containsAny(ram, "8", "16")) {
            categories.add("mid-range laptop");
            categories.add("office laptop");
            categories.add("study laptop");
        }
        else if (containsAny(cpu, "i3", "celeron", "pentium", "ryzen 3", "athlon")) {
            categories.add("budget laptop");
            categories.add("entry-level laptop");
        }
        else if (containsAny(cpu, "ultra", "evo")) {
            categories.add("premium ultrabook");
            categories.add("portable laptop");
        }
        else {
            categories.add("general-purpose laptop");
        }

        log.debug("Found {} categories: {}", categories.size(), categories);
        return categories;
    }
}
