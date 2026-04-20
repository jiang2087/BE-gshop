package com.example.demo.repository.products;

import com.example.demo.models.Product;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @NonNull Page<Product> findAll(@NonNull Pageable pageable);

    Page<Product> findById(String name, Pageable pageable);

    @Query(
            value = "SELECT * FROM product WHERE product_type = :type",
            countQuery = "SELECT COUNT(*) FROM product WHERE product_type = :type",
            nativeQuery = true
    )
    Page<Product> findByType(
            @Param("type") String type,
            Pageable pageable
    );
}
