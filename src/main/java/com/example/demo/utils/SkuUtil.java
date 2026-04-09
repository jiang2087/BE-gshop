package com.example.demo.utils;

import java.text.Normalizer;

public class SkuUtil {
    public static String normalizedProductName(String name){
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFC)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
    public static String normalizedHex(String hex){
        return hex.replace("#", "").toUpperCase();
    }
}
