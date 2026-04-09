package com.example.demo.models.products;

import com.example.demo.models.Product;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "mobiles")
@DiscriminatorValue("MOBILE")
public class Mobile extends Product {
    private String model;
    private Double ScreenSize;
    private String resolution;
    private String camera;
    private String battery; // mAh
    private String dimension;
}
