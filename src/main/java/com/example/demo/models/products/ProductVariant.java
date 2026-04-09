package com.example.demo.models.products;

import com.example.demo.models.Color;
import com.example.demo.models.Order;
import com.example.demo.models.Product;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "color_id", nullable = false)
    Color color;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    @Column(unique = true, nullable = false, updatable = false)
    private String sku;

    @Column(nullable = false)
    private BigDecimal price;

    @Column
    private String image;

    @Min(0)
    @Column(nullable = false)
    private Integer stockQuantity;

    @Version
    private Integer version;

    @JsonIgnore
    @JsonManagedReference
    @OneToMany(mappedBy = "productVariant")
    List<OrderItem> orderItems;
}