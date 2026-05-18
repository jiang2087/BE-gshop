package com.example.demo.utils;


import com.example.demo.dto.request.AttributeDTO;
import com.example.demo.models.Product;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProductUtil {

    public void mapAttributesToEntity(Product product, List<AttributeDTO> attrs) {

        // unwrap proxy
        Product actualProduct = (Product) Hibernate.unproxy(product);

        Class<?> clazz = actualProduct.getClass();

        for (AttributeDTO attr : attrs) {

            log.info("----------------------------------------");
            log.info("Processing attribute");
            log.info("Key   : {}", attr.key());
            log.info("Value : {}", attr.value());

            try {

                Field field = findField(clazz, attr.key());

                log.info("Field found: {}", field.getName());
                log.info("Field type : {}", field.getType().getName());

                field.setAccessible(true);

                Object convertedValue =
                        convertValue(field.getType(), attr.value());

                log.info("Converted value      : {}", convertedValue);

                log.info("Converted value type : {}",
                        convertedValue != null
                                ? convertedValue.getClass().getName()
                                : "null");

                // IMPORTANT
                Object oldValue = field.get(actualProduct);

                log.info("Old value: {}", oldValue);

                // IMPORTANT
                field.set(actualProduct, convertedValue);

            } catch (NoSuchFieldException e) {

                throw new IllegalArgumentException(
                        "Invalid field: " + attr.key(), e);

            } catch (Exception e) {

                log.error("Error while mapping attribute");
                log.error("Field : {}", attr.key());
                log.error("Value : {}", attr.value(), e);

                throw new RuntimeException("Mapping error", e);
            }
        }
    }

    public String toBooleanKeyword(String input) {

        return Arrays.stream(input.trim().split("\s+"))
                .filter(word -> !word.isBlank())
                .map(word -> {
                    // Remove special characters that cause FULLTEXT boolean syntax errors
                    String sanitized = word.replaceAll("[+\\-<>()~*\"@]", "");
                    return sanitized.isBlank() ? "" : "+" + sanitized + "*";
                })
                .filter(term -> !term.isBlank())
                .collect(Collectors.joining(" "));
    }

    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private Object convertValue(Class<?> type, Object value) {
        if (value == null) return null;

        if (type == String.class) return value.toString();

        if (type == Double.class) {
            return Double.valueOf(value.toString());
        }

        if (type == Integer.class) {
            return Integer.valueOf(value.toString());
        }

        if (type == Boolean.class) {
            return Boolean.valueOf(value.toString());
        }

        throw new IllegalArgumentException("Unsupported type: " + type);
    }
}