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

        sb.append(String.format("%s là tivi của %s.\n\n", product.name(), product.brand()));

        sb.append("Mô tả:\n");
        sb.append(product.description()).append("\n\n");

        sb.append("Thông số:\n");
        var attrs = product.productAttributes();
        if (attrs != null) {
            appendIfPresent(sb, "Kích thước", attrs.get("screenSize"));
            appendIfPresent(sb, "Độ phân giải", attrs.get("resolution"));
            appendIfPresent(sb, "Tần số quét", attrs.get("refreshRate"));
            appendIfPresent(sb, "Trọng lượng", attrs.get("weight"));
            appendIfPresent(sb, "Bảo hành", attrs.get("warrantyMonths"));
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

        String resolution = safe(attrs.get("resolution")).toLowerCase();
        Object refreshRateObj = attrs.get("refreshRate");
        Object screenSizeObj = attrs.get("screenSize");
        Object warrantyMonthsObj = attrs.get("warrantyMonths");

        // 4K/8K resolution
        if (resolution.contains("4k") || resolution.contains("uhd") 
                || resolution.contains("8k")) {
            sb.append("- xem phim chất lượng cao\n");
            sb.append("- giải trí gia đình\n");
            sb.append("- xem thể thao\n");
        }

        // High refresh rate
        if (refreshRateObj != null) {
            try {
                int refreshRate = Integer.parseInt(refreshRateObj.toString());
                if (refreshRate >= 120) {
                    sb.append("- chơi game\n");
                    sb.append("- xem thể thao\n");
                    sb.append("- hành động mượt mà\n");
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // Screen size detection
        if (screenSizeObj != null) {
            try {
                double screenSize = Double.parseDouble(screenSizeObj.toString());
                if (screenSize >= 55) {
                    sb.append("- phòng khách rộng\n");
                    sb.append("- rạp phim gia đình\n");
                    sb.append("- xem tập thể\n");
                } else if (screenSize >= 32) {
                    sb.append("- phòng ngủ\n");
                    sb.append("- phòng nhỏ\n");
                    sb.append("- văn phòng\n");
                } else {
                    sb.append("- phòng nhỏ\n");
                    sb.append("- bếp\n");
                    sb.append("- tiết kiệm không gian\n");
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // Full HD
        if (resolution.contains("full hd") || resolution.contains("1080p")) {
            sb.append("- xem phim\n");
            sb.append("- xem truyền hình\n");
        }

        // HD
        if (resolution.contains("hd") && !resolution.contains("full") 
                && !resolution.contains("uhd") && !resolution.contains("4k")) {
            sb.append("- xem truyền hình\n");
            sb.append("- tiết kiệm\n");
        }

        // Long warranty
        if (warrantyMonthsObj != null) {
            try {
                int warrantyMonths = Integer.parseInt(warrantyMonthsObj.toString());
                if (warrantyMonths >= 24) {
                    sb.append("- bảo hành tốt\n");
                    sb.append("- yên tâm sử dụng\n");
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