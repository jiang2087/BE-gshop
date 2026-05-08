package com.example.demo.models;

import com.example.demo.models.products.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(
        name = "product_type",
        discriminatorType = DiscriminatorType.STRING
)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "productType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Laptop.class, name = "LAPTOP"),
        @JsonSubTypes.Type(value = Mobile.class, name = "MOBILE"),
        @JsonSubTypes.Type(value = Television.class, name = "TELEVISION"),
        @JsonSubTypes.Type(value = Watches.class, name = "WATCHES")
})
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String brand;
    private String thumbnail;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @JsonManagedReference
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ProductVariant> productVariants;

    @Transient
    @JsonProperty("productType")
    public String getProductType() {
        DiscriminatorValue dv = this.getClass().getAnnotation(DiscriminatorValue.class);
        return dv != null ? dv.value() : this.getClass().getSimpleName().toUpperCase();
    }
}

