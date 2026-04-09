package com.example.demo.models;


import com.example.demo.models.products.ProductVariant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "order_id")
        },
        indexes = {
                @Index(name = "idx_product", columnList = "product_variant_id"),
                @Index(name = "idx_user", columnList = "user_id")
        }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",  nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id",  nullable = false)
    private ProductVariant productVariant;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer rating;

    private String avatar;

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
