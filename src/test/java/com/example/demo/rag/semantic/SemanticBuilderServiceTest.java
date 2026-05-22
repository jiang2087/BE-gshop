package com.example.demo.rag.semantic;

import com.example.demo.dto.product.ProductDetailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SemanticBuilderServiceTest {

    private SemanticBuilderService semanticBuilderService;

    private List<ProductSemanticBuilder> builders;

    @BeforeEach
    void setUp() {
        builders = List.of(
                new LaptopSemanticBuilder(),
                new MobileSemanticBuilder(),
                new WatchesSemanticBuilder(),
                new TelevisionSemanticBuilder()
        );
        semanticBuilderService = new SemanticBuilderService(builders);
    }

    @Test
    void testBuildersAreInjected() {
        assertNotNull(builders, "Builders list should not be null");
        assertFalse(builders.isEmpty(), "Builders list should not be empty");
        assertEquals(4, builders.size(), "Should have 4 builders");

        Set<String> builderNames = builders.stream()
                .map(builder -> builder.getClass().getSimpleName())
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(builderNames.contains("LaptopSemanticBuilder"));
        assertTrue(builderNames.contains("MobileSemanticBuilder"));
        assertTrue(builderNames.contains("WatchesSemanticBuilder"));
        assertTrue(builderNames.contains("TelevisionSemanticBuilder"));
    }

    @Test
    void testLaptopNaturalLanguageBuilder() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("cpu", "Intel Core i7-12700H");
        attributes.put("ram", "16GB");
        attributes.put("gpu", "NVIDIA RTX 3050");
        attributes.put("storage", "1TB SSD");
        attributes.put("screenSize", "15.6");

        ProductDetailDto product = new ProductDetailDto(
                1L,
                "HP Pavilion 14",
                "Hp",
                "High performance laptop",
                "LAPTOP",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertNotNull(result);
        assertFalse(result.isBlank());

        assertTrue(result.contains("HP Pavilion 14 laptop by Hp."), "Should include laptop heading");
        assertTrue(result.contains("Intel Core i7-12700H processor."), "Should contain CPU sentence");
        assertTrue(result.contains("16GB RAM."), "Should contain RAM sentence");
        assertTrue(result.contains("NVIDIA RTX 3050 graphics."), "Should contain GPU sentence");
        assertTrue(result.contains("1TB SSD storage."), "Should contain storage sentence");
        assertTrue(result.contains("15.6 inch display."), "Should contain normalized screen size sentence");

        assertTrue(result.contains("Best for:"), "Should contain use cases section");
        assertTrue(result.contains("gaming"), "Should identify as gaming laptop");
        assertTrue(result.contains("Category:"), "Should contain category section");
        assertTrue(result.contains("gaming laptop"), "Should classify into gaming category");
    }

    @Test
    void testLaptopGamingBestForCases() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("cpu", "Intel Core i9-12900H");
        attributes.put("ram", "32GB");
        attributes.put("gpu", "NVIDIA RTX 4090");
        attributes.put("storage", "2TB SSD");
        attributes.put("screenSize", "17.3");

        ProductDetailDto product = new ProductDetailDto(
                1L,
                "ASUS ROG Zephyrus G14",
                "ASUS",
                "Ultimate gaming laptop",
                "LAPTOP",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertTrue(result.contains("Best for:"), "Should have best for section");
        assertTrue(result.contains("gaming"), "Should identify for gaming");
        assertTrue(result.contains("graphic design"), "Should identify for graphic design");
        assertTrue(result.contains("video editing"), "Should identify for video editing");
        assertTrue(result.contains("multitasking"), "Should identify for multitasking");
        assertTrue(result.contains("professional work"), "Should identify for professional work");
        assertTrue(result.contains("content creation"), "Should identify for content creation");
    }

    @Test
    void testLaptopPortableBestForCases() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("cpu", "Intel Core i5-1235U");
        attributes.put("ram", "8GB");
        attributes.put("gpu", "Intel Iris Xe");
        attributes.put("storage", "512GB SSD");
        attributes.put("screenSize", "13.3");

        ProductDetailDto product = new ProductDetailDto(
                2L,
                "ASUS VivoBook 13",
                "ASUS",
                "Portable ultrabook",
                "LAPTOP",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertTrue(result.contains("Best for:"), "Should have best for section");
        assertTrue(result.contains("frequent travel"), "Should identify for frequent travel");
        assertTrue(result.contains("portable work"), "Should identify for portable work");
        assertTrue(result.contains("study"), "Should identify for study");
        assertTrue(result.contains("office work"), "Should identify for office work");
        assertTrue(result.contains("web browsing"), "Should identify for web browsing");
    }

    @Test
    void testLaptopWithWhitespaceInScreenSize() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("cpu", "Intel Core i7-12700H");
        attributes.put("ram", "16GB");
        attributes.put("gpu", "NVIDIA RTX 3050");
        attributes.put("storage", "1TB SSD");
        attributes.put("screenSize", "15. 6");

        ProductDetailDto product = new ProductDetailDto(
                2L,
                "HP Pavilion 14",
                "Hp",
                "High performance laptop",
                "LAPTOP",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertNotNull(result);
        assertTrue(result.contains("15.6 inch display"), "Should normalize screenSize by removing whitespace");
        assertTrue(result.contains("Best for:"), "Should still generate use cases");
        assertTrue(result.contains("Category:"), "Should still generate categories");
    }

    @Test
    void testLaptopWithUnicodeWhitespaceInScreenSize() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("cpu", "Apple M2");
        attributes.put("ram", "16GB");
        attributes.put("gpu", "Apple GPU");
        attributes.put("storage", "512GB SSD");
        attributes.put("screenSize", "13.\u00A06");

        ProductDetailDto product = new ProductDetailDto(
                7L,
                "MacBook Air M2",
                "Apple",
                "Thin and light laptop",
                "LAPTOP",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(result.contains("13.6 inch display."), "Should normalize Unicode spaces in screenSize");
        assertFalse(result.contains("13. 6 inch display."), "Should not keep whitespace after decimal point");
    }

    @Test
    void testLaptopBudgetCategory() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("cpu", "Intel Core i3-1115G4");
        attributes.put("ram", "8GB");
        attributes.put("gpu", "Intel UHD Graphics");
        attributes.put("storage", "256GB SSD");
        attributes.put("screenSize", "14");

        ProductDetailDto product = new ProductDetailDto(
                3L,
                "Budget Laptop",
                "Acer",
                "Entry level laptop",
                "LAPTOP",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertTrue(result.contains("budget laptop") || result.contains("entry-level"),
                "Should identify as budget laptop");
        assertTrue(result.contains("study") || result.contains("office work"),
                "Should suggest basic use cases");
    }

    @Test
    void testProductTypeNormalization() {
        assertTrue(semanticBuilderService.hasBuilderFor("LAPTOP"));
        assertTrue(semanticBuilderService.hasBuilderFor("laptop"));
        assertTrue(semanticBuilderService.hasBuilderFor("Laptop"));
        assertTrue(semanticBuilderService.hasBuilderFor("LAPTOPS"));
        assertTrue(semanticBuilderService.hasBuilderFor("PHONE"));
        assertTrue(semanticBuilderService.hasBuilderFor("smartphones"));
        assertTrue(semanticBuilderService.hasBuilderFor("MOBILE"));
        assertTrue(semanticBuilderService.hasBuilderFor("watch"));
        assertTrue(semanticBuilderService.hasBuilderFor("WATCHES"));
        assertTrue(semanticBuilderService.hasBuilderFor("tv"));
        assertTrue(semanticBuilderService.hasBuilderFor("TELEVISION"));
        assertFalse(semanticBuilderService.hasBuilderFor("TABLET"));
        assertFalse(semanticBuilderService.hasBuilderFor(""));
        assertFalse(semanticBuilderService.hasBuilderFor(null));
    }

    @Test
    void testMobileNaturalLanguageBuilder() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("processor", "Snapdragon 8 Gen 2");
        attributes.put("ram", "12GB");
        attributes.put("storage", "256GB");
        attributes.put("screenSize", "6.7");

        ProductDetailDto product = new ProductDetailDto(
                4L,
                "Samsung Galaxy S23",
                "Samsung",
                "Flagship smartphone",
                "MOBILE",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertNotNull(result);
        assertTrue(result.contains("Samsung Galaxy S23"), "Should contain product name");
        assertTrue(result.contains("Samsung"), "Should contain brand");
    }

    @Test
    void testMobilePremiumBestForCases() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("camera", "200MP");
        attributes.put("battery", "5000mAh");
        attributes.put("resolution", "4K");
        attributes.put("screenSize", "6.7");

        ProductDetailDto product = new ProductDetailDto(
                5L,
                "Samsung Galaxy S24 Ultra",
                "Samsung",
                "Premium smartphone",
                "MOBILE",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertTrue(result.contains("Best for:"), "Should have best for section");
        assertTrue(result.contains("high-quality photography"), "Should identify for photography");
        assertTrue(result.contains("high-quality video recording"), "Should identify for video recording");
        assertTrue(result.contains("content creation"), "Should identify for content creation");
        assertTrue(result.contains("long battery life"), "Should identify for long battery");
        assertTrue(result.contains("entertainment"), "Should identify for entertainment");
        assertTrue(result.contains("Category:"), "Should have category section");
        assertTrue(result.contains("premium smartphone") || result.contains("flagship phone"), 
                "Should identify as premium phone");
    }

    @Test
    void testMobileBudgetBestForCases() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("camera", "12MP");
        attributes.put("battery", "3500mAh");
        attributes.put("resolution", "Full HD");
        attributes.put("screenSize", "6.0");

        ProductDetailDto product = new ProductDetailDto(
                6L,
                "Budget Phone",
                "Redmi",
                "Budget smartphone",
                "MOBILE",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertTrue(result.contains("Best for:"), "Should have best for section");
        assertTrue(result.contains("daily photography"), "Should identify for daily photography");
        assertTrue(result.contains("compact one-hand use"), "Should identify for one-hand use");
        assertTrue(result.contains("Category:"), "Should have category section");
        assertTrue(result.contains("entry-level smartphone") || result.contains("budget phone"),
                "Should identify as budget phone");
    }

    @Test
    void testMobileGamingBestForCases() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("camera", "50MP");
        attributes.put("battery", "6000mAh");
        attributes.put("resolution", "2K");
        attributes.put("screenSize", "6.8");

        ProductDetailDto product = new ProductDetailDto(
                7L,
                "Gaming Phone",
                "OnePlus",
                "Gaming focused smartphone",
                "MOBILE",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertTrue(result.contains("Best for:"), "Should have best for section");
        assertTrue(result.contains("gaming"), "Should identify for gaming");
        assertTrue(result.contains("high-quality streaming"), "Should identify for streaming");
        assertTrue(result.contains("entertainment"), "Should identify for entertainment");
    }

    @Test
    void testNullProductAttributes() {
        ProductDetailDto product = new ProductDetailDto(
                5L,
                "Test Product",
                "Test Brand",
                "Test description",
                "LAPTOP",
                null,
                null,
                null,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(result.contains("Test Product"), "Should still contain product name");
        assertFalse(result.contains("Best for:"), "Should not generate use cases when attributes null");
        assertFalse(result.contains("Category:"), "Should not generate category when attributes null");
    }

    @Test
    void testUnsupportedProductTypeUsesDefaultBuilder() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("color", "Red");

        ProductDetailDto product = new ProductDetailDto(
                6L,
                "Unknown Product",
                "Unknown Brand",
                "Some description",
                "TABLET",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertNotNull(result);
        assertTrue(result.contains("Unknown Product"), "Should include product name");
        assertTrue(result.contains("Unknown Brand"), "Should include brand in default natural language text");
        assertTrue(result.contains("Some description"), "Should include description in default natural language text");
        assertTrue(result.contains("color: Red"), "Should include product attributes in default natural language text");
        assertFalse(result.contains("Best for:"), "Default builder should not generate use cases section");
        assertFalse(result.contains("Category:"), "Default builder should not generate category section");
    }

    @Test
    void testWatchesPremiumBestForCases() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("material", "titanium");
        attributes.put("gender", "male");
        attributes.put("screenSize", "1.9");
        attributes.put("gps", true);
        attributes.put("batteryLife", "14");
        attributes.put("weight", "25g");

        ProductDetailDto product = new ProductDetailDto(
                8L,
                "Apple Watch Ultra",
                "Apple",
                "Premium sports smartwatch",
                "WATCHES",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertTrue(result.contains("Best for:"), "Should have best for section");
        assertTrue(result.contains("running"), "Should identify for running");
        assertTrue(result.contains("hiking"), "Should identify for hiking");
        assertTrue(result.contains("outdoor activities"), "Should identify for outdoor activities");
        assertTrue(result.contains("premium style"), "Should identify for premium style");
        assertTrue(result.contains("long-term use"), "Should identify for long-term use");
        assertTrue(result.contains("men's fitness tracking"), "Should identify for men's fitness");
        assertTrue(result.contains("Category:"), "Should have category section");
        assertTrue(result.contains("premium smartwatch") || result.contains("luxury smartwatch"),
                "Should identify as premium watch");
    }

    @Test
    void testWatchesSportsBestForCases() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("material", "stainless steel");
        attributes.put("gender", "female");
        attributes.put("screenSize", "1.6");
        attributes.put("gps", true);
        attributes.put("batteryLife", "7");
        attributes.put("weight", "30g");

        ProductDetailDto product = new ProductDetailDto(
                9L,
                "Garmin Forerunner",
                "Garmin",
                "Sports smartwatch",
                "WATCHES",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertTrue(result.contains("Best for:"), "Should have best for section");
        assertTrue(result.contains("running"), "Should identify for running");
        assertTrue(result.contains("Category:"), "Should have category section");
        assertTrue(result.contains("sports smartwatch") || result.contains("fitness watch"),
                "Should identify as sports watch");
    }

    @Test
    void testWatchesBudgetBestForCases() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("material", "plastic");
        attributes.put("gender", "unisex");
        attributes.put("screenSize", "1.3");
        attributes.put("gps", false);
        attributes.put("batteryLife", "3");
        attributes.put("weight", "40g");

        ProductDetailDto product = new ProductDetailDto(
                10L,
                "Budget Watch",
                "Generic",
                "Entry level smartwatch",
                "WATCHES",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertTrue(result.contains("Best for:"), "Should have best for section");
        assertTrue(result.contains("health tracking"), "Should identify for health tracking");
        assertTrue(result.contains("activity tracking"), "Should identify for activity tracking");
        assertTrue(result.contains("unisex fitness tracking"), "Should identify for unisex fitness");
        assertTrue(result.contains("Category:"), "Should have category section");
        assertTrue(result.contains("entry-level smartwatch") || result.contains("basic smartwatch"),
                "Should identify as budget watch");
    }

    @Test
    void testTelevisionPremiumBestForCases() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("screenSize", "75");
        attributes.put("resolution", "8K");
        attributes.put("refreshRate", "120");
        attributes.put("weight", "40kg");
        attributes.put("warrantyMonths", "36");

        ProductDetailDto product = new ProductDetailDto(
                11L,
                "Samsung QN75QN900D",
                "Samsung",
                "Premium 8K TV",
                "TELEVISION",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertTrue(result.contains("Best for:"), "Should have best for section");
        assertTrue(result.contains("high-quality movie watching"), "Should identify for movie watching");
        assertTrue(result.contains("gaming"), "Should identify for gaming");
        assertTrue(result.contains("sports viewing"), "Should identify for sports");
        assertTrue(result.contains("large living room"), "Should identify for large room");
        assertTrue(result.contains("worry-free long-term use"), "Should identify for long warranty");
        assertTrue(result.contains("Category:"), "Should have category section");
        assertTrue(result.contains("premium television") || result.contains("high-end TV"),
                "Should identify as premium TV");
    }

    @Test
    void testTelevision4KBestForCases() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("screenSize", "55");
        attributes.put("resolution", "4K UHD");
        attributes.put("refreshRate", "60");
        attributes.put("weight", "25kg");
        attributes.put("warrantyMonths", "24");

        ProductDetailDto product = new ProductDetailDto(
                12L,
                "LG OLED55C3PUA",
                "LG",
                "4K OLED TV",
                "TELEVISION",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertTrue(result.contains("Best for:"), "Should have best for section");
        assertTrue(result.contains("high-quality movie watching"), "Should identify for movies");
        assertTrue(result.contains("home entertainment"), "Should identify for entertainment");
        assertTrue(result.contains("Category:"), "Should have category section");
        assertTrue(result.contains("4K TV") || result.contains("mid-range television"),
                "Should identify as 4K TV");
    }

    @Test
    void testTelevisionBudgetBestForCases() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("screenSize", "32");
        attributes.put("resolution", "Full HD");
        attributes.put("refreshRate", "60");
        attributes.put("weight", "5kg");
        attributes.put("warrantyMonths", "12");

        ProductDetailDto product = new ProductDetailDto(
                13L,
                "Budget TV 32inch",
                "Generic",
                "Budget Full HD TV",
                "TELEVISION",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertTrue(result.contains("Best for:"), "Should have best for section");
        assertTrue(result.contains("movies and TV shows"), "Should identify for shows");
        assertTrue(result.contains("Category:"), "Should have category section");
        assertTrue(result.contains("Full HD TV") || result.contains("budget television"),
                "Should identify as budget Full HD TV");
    }

    @Test
    void testTelevisionGamingBestForCases() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("screenSize", "65");
        attributes.put("resolution", "4K");
        attributes.put("refreshRate", "144");
        attributes.put("weight", "30kg");
        attributes.put("warrantyMonths", "24");

        ProductDetailDto product = new ProductDetailDto(
                14L,
                "Gaming TV 65inch",
                "MSI",
                "High refresh rate gaming TV",
                "TELEVISION",
                null,
                null,
                attributes,
                List.of()
        );

        String result = semanticBuilderService.buildSemanticText(product);

        assertTrue(result.contains("Best for:"), "Should have best for section");
        assertTrue(result.contains("gaming"), "Should identify for gaming");
        assertTrue(result.contains("fast-action content"), "Should identify for fast action");
        assertTrue(result.contains("Category:"), "Should have category section");
    }

    @Test
    void testBuildSemanticTextBatch() {
        ProductDetailDto laptop = new ProductDetailDto(
                11L, "Dell G15", "Dell", "Gaming laptop", "LAPTOP",
                null, null,
                Map.of("cpu", "Intel Core i7", "ram", "16GB", "gpu", "RTX 4060"),
                List.of()
        );
        ProductDetailDto unknown = new ProductDetailDto(
                12L, "Generic Tablet", "BrandX", "Simple tablet", "TABLET",
                null, null,
                Map.of("color", "Black"),
                List.of()
        );

        List<String> results = semanticBuilderService.buildSemanticTextBatch(List.of(laptop, unknown));

        assertEquals(2, results.size(), "Batch output size should match input size");
        assertTrue(results.get(0).contains("Dell G15 laptop by Dell."), "First item should use laptop builder");
        assertTrue(results.get(1).contains("Generic Tablet"), "Second item should use default builder text");
    }

    @Test
    void testBuildSemanticTextBatchWithNullOrEmptyInput() {
        assertEquals(List.of(), semanticBuilderService.buildSemanticTextBatch(null));
        assertEquals(List.of(), semanticBuilderService.buildSemanticTextBatch(List.of()));
    }

    @Test
    void testGetSupportedProductTypes() {
        List<String> supported = semanticBuilderService.getSupportedProductTypes();

        assertEquals(4, supported.size());
        assertEquals(List.of("LAPTOP", "MOBILE", "WATCHES", "TELEVISION"), supported);
    }

    @Test
    void testBuildSemanticTextWithNullProduct() {
        assertEquals("", semanticBuilderService.buildSemanticText(null));
    }
}

