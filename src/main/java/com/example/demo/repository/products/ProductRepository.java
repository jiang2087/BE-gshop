package com.example.demo.repository.products;

import com.example.demo.models.Product;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @NonNull Page<Product> findAll(@NonNull Pageable pageable);

    @Query("""
                SELECT p.name FROM Product p WHERE p.id in :ids
            """)
    List<String> findByIds(@Param("ids") List<Long> ids);

    @Query("""
                SELECT p
                FROM Product p
                JOIN p.productVariants v
                WHERE v.active = true
                  AND (:types IS NULL OR TYPE(p) IN :types)
                GROUP BY p
                HAVING MIN(v.price) BETWEEN :min AND :max
            """)
    Page<Product> findByCategoryAndPriceRange(
            @Param("types") List<? extends Class<? extends Product>> types,
            @Param("min") BigDecimal min,
            @Param("max") BigDecimal max,
            Pageable pageable
    );

    @Query("""
                SELECT p
                FROM Product p
                JOIN p.productVariants v
                WHERE v.active = true
                GROUP BY p.id
                HAVING MIN(v.price) BETWEEN :min AND :max
            """)
    Page<Product> findByPriceRange(
            @Param("min") BigDecimal min,
            @Param("max") BigDecimal max,
            Pageable pageable
    );

    @Query("SELECT TYPE(p), COUNT(p) FROM Product p WHERE TYPE(p) IN :types GROUP BY TYPE(p)")
    List<Object[]> countByType(@Param("types") List<? extends Class<? extends Product>> types);

    @Query("SELECT p FROM Product p WHERE TYPE(p) IN :type")
    Page<Product> findByType(@Param("type") List<? extends Class<? extends Product>> type, Pageable pageable);
}
