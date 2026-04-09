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
@Table(name = "home_appliances")
@DiscriminatorValue("HOME_APPLIANCE")
public class HomeAppliance extends Product {
    private String capacity;
    private String model;
    private String dimension;
    private Double weight;
    private Integer warrantyMonths;
    private Integer power;
}
