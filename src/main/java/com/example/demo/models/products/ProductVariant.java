package com.example.demo.models.products;

import com.example.demo.models.Color;
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
@Table(indexes = {
        @Index(name = "idx_product_price", columnList = "product_id, price"),
        @Index(name = "idx_product_stock", columnList = "product_id, stock_quantity")
})
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "color_id", nullable = false)
    Color color;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(nullable = false)
    private Boolean isDefault = false;

    @Column(nullable = false)
    private Boolean active = true;

    @Version
    private Integer version;

    @JsonIgnore
    @JsonManagedReference
    @OneToMany(mappedBy = "productVariant")
    List<OrderItem> orderItems;
}