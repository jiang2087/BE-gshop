package com.example.demo.repository.products;

import com.example.demo.dto.response.WishlistResponse;
import com.example.demo.models.products.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    @Query("""
            SELECT new com.example.demo.dto.response.WishlistResponse(
                pv.id,
                p.id,
                :userId,
                pv.image,
                pv.price,
                p.name,
                CASE WHEN pv.stockQuantity > 0 THEN true ELSE false END
            )
            FROM WishlistItem w
            JOIN w.productVariant pv
            JOIN pv.product p
            WHERE w.user.id = :userId
            """)
    List<WishlistResponse> findByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndProductVariantId(Long userId, Long productVariantId);

    void deleteByUserIdAndProductVariantId(Long userId, Long productVariantId);
}
