package com.example.demo.repository.products;

import com.example.demo.dto.response.TopProductProjection;
import com.example.demo.models.products.ProductVariant;
import jakarta.persistence.SqlResultSetMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    boolean existsBySku(String sku);

    Optional<ProductVariant> findBySku(String sku);


    @Query("""
                SELECT pv.sku
                FROM ProductVariant pv
                WHERE pv.id IN :ids
            """)
    List<String> findAllSkuByIds(@Param("ids") List<Long> ids);

    @Query("""
            SELECT DISTINCT pv
            FROM ProductVariant pv
            JOIN FETCH pv.product p
            LEFT JOIN FETCH pv.color c
            WHERE pv.id IN :variantIds
            """)
    List<ProductVariant> findAllWithProductByIdIn(@Param("variantIds") Collection<Long> variantIds);

    @Query("""
            SELECT pv
            FROM ProductVariant pv
            LEFT JOIN FETCH pv.color
            WHERE pv.product.id IN :productIds
            """)
    List<ProductVariant> findAllByProductIdInWithColor(@Param("productIds") Collection<Long> productIds);

    @Query("SELECT COUNT(v) FROM ProductVariant v")
    long countProductVariants();

    @Query("""
            SELECT pv
            FROM ProductVariant pv
            JOIN FETCH pv.product p
            LEFT JOIN FETCH pv.color c
            WHERE pv.active = true
            """)
    List<ProductVariant> findAllActiveWithProductAndColor();

    @Query(value = """
    SELECT
        p.thumbnail as image,
        p.name AS productName,
        p.product_type AS productType,
        AVG(oi.price) AS price,
        SUM(oi.quantity) AS sold,
        SUM(oi.quantity * oi.price) AS profit
    FROM order_item oi
    JOIN orders o ON oi.order_id = o.id
    JOIN product_variant pv ON oi.product_variant_id = pv.id
    JOIN products p ON pv.product_id = p.id
    WHERE o.status = 'COMPLETED'
    GROUP BY p.id, p.name, p.product_type
    ORDER BY profit DESC
""", nativeQuery = true)
    Page<TopProductProjection> getTopProducts(Pageable pageable);

    Optional<ProductVariant> findByProductIdAndIsDefaultTrue(Long productId);
    Optional<ProductVariant> findFirstByProductIdAndIsDefaultFalse(Long productId);
    @Modifying
    @Query("""
    UPDATE ProductVariant v
    SET v.isDefault = false
    WHERE v.product.id = :productId
""")
    void clearDefaultByProductId(@Param("productId") Long productId);
}
