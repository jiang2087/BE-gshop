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
                p.id,
                :userId,
                p.thumbnail,
                COALESCE(
                    (SELECT MIN(v.price)
                     FROM ProductVariant v
                     WHERE v.product.id = p.id),
                    0
                ),
            
                p.name,
            
                CASE WHEN EXISTS (
                    SELECT 1 FROM ProductVariant v2
                    WHERE v2.product.id = p.id
                      AND v2.stockQuantity > 0
                ) THEN true ELSE false END
            )
            FROM WishlistItem w
            JOIN w.product p
            WHERE w.user.id = :userId
            """)
    List<WishlistResponse> findByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}