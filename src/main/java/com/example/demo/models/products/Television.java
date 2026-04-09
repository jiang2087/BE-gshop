package com.example.demo.models.products;

import com.example.demo.models.Product;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "televisions")
@DiscriminatorValue("TELEVISION")
@Getter
@Setter
public class Television extends Product {
    private String resolution; // HD, Full HD, 4K
    private Integer refreshRate; // Hz
    private Double screenSize;
    private Double weight;
    private Integer warrantyMonths;
}
