package com.example.demo.models.products;

import com.example.demo.models.Product;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "watches")
@DiscriminatorValue("WATCH")
@Setter
@Getter
public class Watches extends Product {
    private String model;
    private String gender;
    private Double screenSize;
    private Boolean gps;
    private Double batteryLife;
    private String weight;
    private String material;
}
