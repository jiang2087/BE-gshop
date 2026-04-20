package com.example.demo.models.products;

import com.example.demo.models.Cart;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Entity
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"cart_id", "product_variant_id"}
        ),
        indexes = {
                @Index(name = "idx_cart_item_cart", columnList = "cart_id")
        })
public class CartItem {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "cart_id", nullable = false)
        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
        private Cart cart;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "product_variant_id", nullable = false)
        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
        private ProductVariant productVariant;

        private BigDecimal price;

        private Integer quantity;

        private LocalDateTime createdAt;

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof CartItem that)) return false;

            return productVariant != null && that.productVariant != null
                        && Objects.equals(productVariant.getId(), that.productVariant.getId());
        }

        @Override
        public int hashCode() {
                return Objects.hash(productVariant.getId());
        }
}
