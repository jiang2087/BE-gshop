package com.example.demo.repository;

import com.example.demo.models.Discount;
import com.example.demo.models.products.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    @Query("""
            SELECT d FROM Discount d
            WHERE (:active IS NULL OR d.active = :active)
            AND (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<Discount> findByActiveAndName(
            @Param("active") Boolean active,
            @Param("name") String name,
            Pageable pageable
    );

    @Query("""
            SELECT d FROM Discount d
            JOIN d.productVariants p
            WHERE p.id = :productId
            AND d.active = true
            AND :now BETWEEN d.startDate AND d.endDate
            """)
    List<Discount> findActiveDiscountByProduct(Long productId, LocalDateTime now);

    @Query("""
            SELECT pv
            FROM ProductVariant pv
            WHERE pv.id NOT IN (
                SELECT dpv.id
                FROM Discount d
                JOIN d.productVariants dpv
                WHERE d.id = :discountId
            )
            AND (:sku IS NULL OR LOWER(pv.sku) LIKE LOWER(CONCAT('%', :sku, '%')))
            """)
    Page<ProductVariant> findProductVariantsNotInDiscount(
            @Param("discountId") Long discountId,
            @Param("sku") String sku,
            Pageable pageable
    );

    @Query("""
            SELECT pv
            FROM Discount d
            JOIN d.productVariants pv
            WHERE d.id = :discountId
            AND (:sku IS NULL OR LOWER(pv.sku) LIKE LOWER(CONCAT('%', :sku, '%')))
            """)
    Page<ProductVariant> findProductVariantsInDiscount(
            @Param("discountId") Long discountId,
            @Param("sku") String sku,
            Pageable pageable
    );
}
