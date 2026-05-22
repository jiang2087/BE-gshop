package com.example.demo.rag.semantic;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for natural-language builders providing common utility methods
 */
public abstract class AbstractSemanticBuilder implements ProductSemanticBuilder {

    /**
     * Safely extract string value from attribute map
     */
    protected String safe(Object value) {
        return value != null ? value.toString().trim() : "";
    }

    /**
     * Append text to StringBuilder if value is present and non-empty
     */
    protected void appendIfPresent(StringBuilder sb, Object value, String text) {
        if (value != null && !value.toString().trim().isEmpty()) {
            sb.append(text);
        }
    }

    /**
     * Parse integer from object, return default value on error
     */
    protected int parseInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parse double from object, return default value on error
     */
    protected double parseDouble(Object value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            String cleaned = stripAllSpaces(value.toString());
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parse boolean from object, return default value on error
     */
    protected boolean parseBoolean(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.toString().trim());
    }

    /**
     * Normalize screen size by removing whitespace
     */
    protected String normalizeScreenSize(Object screenSize) {
        if (screenSize == null) {
            return "";
        }
        return stripAllSpaces(screenSize.toString());
    }

    /**
     * Remove ASCII and Unicode space separators (NBSP, narrow NBSP, etc.).
     */
    protected String stripAllSpaces(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value
                .replaceAll("\\s+", "")
                .replaceAll("\\p{Z}+", "");
    }

    /**
     * Append use cases section to StringBuilder
     */
    protected void appendUseCasesSection(StringBuilder sb, Set<String> useCases) {
        if (useCases != null && !useCases.isEmpty()) {
            sb.append("\nBest for ")
                    .append(joinAsNaturalList(useCases))
                    .append(".\n");
        }
    }

    /**
     * Append category section to StringBuilder
     */
    protected void appendCategorySection(StringBuilder sb, Set<String> categories) {
        if (categories != null && !categories.isEmpty()) {
            sb.append("\nThis product belongs to categories such as ")
                    .append(joinAsNaturalList(categories))
                    .append(".\n");
        }
    }

    /**
     * Join set items as natural language list: "A", "A and B", "A, B, and C"
     */
    protected String joinAsNaturalList(Set<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }

        String[] values = items.toArray(new String[0]);
        if (values.length == 1) {
            return values[0];
        }
        if (values.length == 2) {
            return values[0] + " and " + values[1];
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                result.append(i == values.length - 1 ? ", and " : ", ");
            }
            result.append(values[i]);
        }
        return result.toString();
    }

    /**
     * Create a new LinkedHashSet for maintaining insertion order
     */
    protected Set<String> createOrderedSet() {
        return new LinkedHashSet<>();
    }

    /**
     * Check if attributes map is null or empty
     */
    protected boolean hasAttributes(Map<String, Object> attrs) {
        return attrs != null && !attrs.isEmpty();
    }

    /**
     * Get attribute value safely
     */
    protected Object getAttribute(Map<String, Object> attrs, String key) {
        return attrs != null ? attrs.get(key) : null;
    }

    /**
     * Check if string contains any of the given keywords (case-insensitive)
     */
    protected boolean containsAny(String text, String... keywords) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String lowerText = text.toLowerCase();
        for (String keyword : keywords) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract numeric value from string (e.g., "16GB" -> 16)
     */
    protected int extractNumber(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(text.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
