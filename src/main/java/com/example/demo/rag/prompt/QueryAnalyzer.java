package com.example.demo.rag.prompt;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class QueryAnalyzer {

    // Biểu thức chính quy để tìm các cụm từ chỉ số lượng như "3 sản phẩm", "top 5", "cho tôi 2 cái", v.v.
    private static final Pattern LIMIT_PATTERN = Pattern.compile(
            "(?i)(?:top\\s*(\\d+)|(\\d+)\\s*(?:sản phẩm|món|cái|chiếc|áo|laptop|đồng hồ|máy tính|tivi)|cho\\s*(?:tôi|mình|xin)\\s*(\\d+))"
    );


    public int extractProductLimit(String query, int defaultLimit) {
        if (query == null || query.isBlank()) {
            return defaultLimit;
        }

        Matcher matcher = LIMIT_PATTERN.matcher(query);
        if (matcher.find()) {
            // Kiểm tra xem nhóm nào khớp và trả về số tương ứng
            if (matcher.group(1) != null) {
                return Integer.parseInt(matcher.group(1)); // Khớp "top X"
            } else if (matcher.group(2) != null) {
                return Integer.parseInt(matcher.group(2)); // Khớp "X sản phẩm"
            } else if (matcher.group(3) != null) {
                return Integer.parseInt(matcher.group(3)); // Khớp "cho tôi X"
            }
        }


        Pattern simpleNumberPattern = Pattern.compile("(?<!\\d)(\\d+)(?!\\d)");
        Matcher simpleMatcher = simpleNumberPattern.matcher(query);
        while (simpleMatcher.find()) {
            int num = Integer.parseInt(simpleMatcher.group(1));
            if (num > 0 && num <= 20) {
                return num;
            }
        }

        return defaultLimit;
    }
}
