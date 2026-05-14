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

        sb.append(String.format("%s là laptop của %s.\n\n", product.name(), product.brand()));

        sb.append("Mô tả:\n");
        sb.append(product.description()).append("\n\n");

        sb.append("Cấu hình:\n");
        var attrs = product.productAttributes();
        if (attrs != null) {
            appendIfPresent(sb, "CPU", attrs.get("cpu"));
            appendIfPresent(sb, "RAM", attrs.get("ram"));
            appendIfPresent(sb, "GPU", attrs.get("gpu"));
            appendIfPresent(sb, "Storage", attrs.get("storage"));
            appendIfPresent(sb, "Màn hình", attrs.get("screenSize"));
            appendIfPresent(sb, "Độ phân giải", attrs.get("resolution"));
        }

        // Dynamic use-case detection
        appendUseCases(sb, attrs);

        return sb.toString();
    }

    private void appendUseCases(StringBuilder sb, java.util.Map<String, Object> attrs) {
        if (attrs == null) {
            return;
        }

        sb.append("\nPhù hợp cho:\n");

        String gpu = safe(attrs.get("gpu")).toLowerCase();
        String ram = safe(attrs.get("ram")).toLowerCase();
        Object screenSizeObj = attrs.get("screenSize");

        if (gpu.contains("rtx") || gpu.contains("gtx")) {
            sb.append("- gaming\n");
            sb.append("- đồ họa\n");
            sb.append("- render video\n");
        }

        if (ram.contains("16") || ram.contains("32")) {
            sb.append("- đa nhiệm\n");
        }

        if (gpu.contains("iris") || gpu.contains("uhd")) {
            sb.append("- văn phòng\n");
            sb.append("- học tập\n");
        }

        if (screenSizeObj != null) {
            try {
                double screenSize = Double.parseDouble(screenSizeObj.toString());
                if (screenSize <= 14) {
                    sb.append("- di chuyển nhiều\n");
                    sb.append("- làm việc linh hoạt\n");
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private void appendIfPresent(StringBuilder sb, String label, Object value) {
        if (value != null && !value.toString().trim().isEmpty()) {
            sb.append(label).append(": ").append(value).append("\n");
        }
    }

    private String safe(Object value) {
        return value != null ? value.toString() : "";
    }
}