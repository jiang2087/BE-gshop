package com.example.demo.models.products;

import com.example.demo.models.Product;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "laptops")
@DiscriminatorValue("LAPTOP")
@Getter
@Setter
public class Laptop extends Product {
    private String cpu;
    private String ram;
    private String storage;
    private String gpu;
    private String resolution;
    private Double screenSize;
    private String dimension;
}
