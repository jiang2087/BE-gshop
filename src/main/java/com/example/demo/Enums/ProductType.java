package com.example.demo.Enums;

import com.example.demo.models.Product;
import com.example.demo.models.products.Laptop;
import com.example.demo.models.products.Mobile;
import com.example.demo.models.products.Television;
import com.example.demo.models.products.Watches;
import lombok.Getter;

@Getter
public enum ProductType {
    LAPTOP(Laptop.class),
    WATCH(Watches.class),
    MOBILE(Mobile.class),
    TELEVISION(Television.class);

    private final Class<? extends Product> clazz;

    ProductType(Class<? extends Product> clazz) {
        this.clazz = clazz;
    }

}