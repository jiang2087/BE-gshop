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

        sb.append(String.format("%s là điện thoại của %s.\n\n", product.name(), product.brand()));

        sb.append("Mô tả:\n");
        sb.append(product.description()).append("\n\n");

        sb.append("Thông số:\n");
        var attrs = product.productAttributes();
        if (attrs != null) {
            appendIfPresent(sb, "Model", attrs.get("model"));
            appendIfPresent(sb, "Màn hình", attrs.get("screenSize"));
            appendIfPresent(sb, "Độ phân giải", attrs.get("resolution"));
            appendIfPresent(sb, "Camera", attrs.get("camera"));
            appendIfPresent(sb, "Pin", attrs.get("battery"));
            appendIfPresent(sb, "Kích thước", attrs.get("dimension"));
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

        String camera = safe(attrs.get("camera")).toLowerCase();
        String battery = safe(attrs.get("battery")).toLowerCase();
        String resolution = safe(attrs.get("resolution")).toLowerCase();
        Object screenSizeObj = attrs.get("screenSize");

        // High-end camera detection
        if (camera.contains("108mp") || camera.contains("200mp") 
                || camera.contains("50mp") || camera.contains("64mp")) {
            sb.append("- chụp ảnh chuyên nghiệp\n");
            sb.append("- quay video chất lượng cao\n");
            sb.append("- content creator\n");
        }

        // Large battery detection
        if (battery.contains("5000") || battery.contains("6000") 
                || battery.contains("7000")) {
            sb.append("- sử dụng lâu dài\n");
            sb.append("- đi lại nhiều\n");
        }

        // High resolution display
        if (resolution.contains("2k") || resolution.contains("4k") 
                || resolution.contains("quad hd") || resolution.contains("qhd")) {
            sb.append("- xem phim chất lượng cao\n");
            sb.append("- chơi game\n");
        }

        // Screen size detection
        if (screenSizeObj != null) {
            try {
                double screenSize = Double.parseDouble(screenSizeObj.toString());
                if (screenSize >= 6.5) {
                    sb.append("- giải trí\n");
                    sb.append("- xem video\n");
                } else if (screenSize <= 6.0) {
                    sb.append("- gọn nhẹ\n");
                    sb.append("- dễ cầm nắm\n");
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