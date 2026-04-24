package com.example.demo.repository.products;

import com.example.demo.models.products.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
