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

        sb.append(String.format("%s là đồng hồ thông minh của %s.\n\n", product.name(), product.brand()));

        sb.append("Mô tả:\n");
        sb.append(product.description()).append("\n\n");

        sb.append("Thông số:\n");
        var attrs = product.productAttributes();
        if (attrs != null) {
            appendIfPresent(sb, "Model", attrs.get("model"));
            appendIfPresent(sb, "Giới tính", attrs.get("gender"));
            appendIfPresent(sb, "Màn hình", attrs.get("screenSize"));
            
            Object gpsObj = attrs.get("gps");
            if (gpsObj != null) {
                boolean hasGps = Boolean.parseBoolean(gpsObj.toString());
                sb.append("GPS: ").append(hasGps ? "Có" : "Không").append("\n");
            }
            
            appendIfPresent(sb, "Thời lượng pin", attrs.get("batteryLife"));
            appendIfPresent(sb, "Trọng lượng", attrs.get("weight"));
            appendIfPresent(sb, "Chất liệu", attrs.get("material"));
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

        String material = safe(attrs.get("material")).toLowerCase();
        String gender = safe(attrs.get("gender")).toLowerCase();
        Object gpsObj = attrs.get("gps");
        Object batteryLifeObj = attrs.get("batteryLife");
        Object screenSizeObj = attrs.get("screenSize");

        // GPS enabled
        if (gpsObj != null && Boolean.parseBoolean(gpsObj.toString())) {
            sb.append("- chạy bộ\n");
            sb.append("- đi bộ đường dài\n");
            sb.append("- leo núi\n");
            sb.append("- thể thao ngoài trời\n");
        }

        // Long battery life
        if (batteryLifeObj != null) {
            try {
                double batteryLife = Double.parseDouble(batteryLifeObj.toString());
                if (batteryLife >= 7) {
                    sb.append("- sử dụng lâu dài\n");
                    sb.append("- đi du lịch\n");
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // Premium materials
        if (material.contains("titanium") || material.contains("sapphire") 
                || material.contains("ceramic")) {
            sb.append("- sang trọng\n");
            sb.append("- bền bỉ\n");
            sb.append("- cao cấp\n");
        }

        // Stainless steel
        if (material.contains("stainless") || material.contains("thép")) {
            sb.append("- phong cách thể thao\n");
            sb.append("- bền bỉ\n");
        }

        // Large screen
        if (screenSizeObj != null) {
            try {
                double screenSize = Double.parseDouble(screenSizeObj.toString());
                if (screenSize >= 1.7) {
                    sb.append("- dễ xem thông tin\n");
                    sb.append("- hiển thị rõ ràng\n");
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // Gender specific
        if (!gender.isEmpty()) {
            if (gender.contains("nam") || gender.contains("male")) {
                sb.append("- nam giới\n");
            } else if (gender.contains("nữ") || gender.contains("female")) {
                sb.append("- nữ giới\n");
            } else if (gender.contains("unisex")) {
                sb.append("- nam nữ\n");
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