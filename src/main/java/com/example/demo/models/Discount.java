package com.example.demo.models;

import com.example.demo.Enums.DiscountType;
import com.example.demo.models.products.ProductVariant;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "discounts")
@Data
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private DiscountType type;

    private Double value; 

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Boolean active;

    @ManyToMany
    @JoinTable(
        name = "discount_products",
        joinColumns = @JoinColumn(name = "discount_id"),
        inverseJoinColumns = @JoinColumn(name = "product_variant_id")
    )
    private List<ProductVariant> productVariants;
}