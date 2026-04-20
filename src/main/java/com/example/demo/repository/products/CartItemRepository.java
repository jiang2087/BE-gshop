package com.example.demo.repository.products;

import com.example.demo.dto.response.CartItemResponse;
import com.example.demo.models.products.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductVariantId(Long cartId, Long variantId);

    List<CartItem> findByCartId(Long cartId);

    void deleteByCartId(Long cartId);

    @Modifying
    @Query("""
                UPDATE CartItem ci
                SET ci.quantity = ci.quantity + :quantity
                WHERE ci.cart.id = :cartId
                  AND ci.productVariant.id = :variantId
            """)
    int increaseQuantity(@Param("cartId") Long cartId,
                         @Param("variantId") Long variantId,
                         @Param("quantity") Integer quantity);

    @Query("""
    SELECT new com.example.demo.dto.response.CartItemResponse(
        c.id,
        ci.id,
        pv.id,
        pv.image,
        pv.sku,
        co.hexCode,
        co.name,
        ci.price,
        ci.quantity,
        c.cartKey
    )
    FROM CartItem ci
    JOIN ci.cart c
    JOIN ci.productVariant pv
    JOIN pv.color co
    WHERE c.id = :cartId
""")
    List<CartItemResponse> getCartItemsByCartId(@Param("cartId") Long cartId);

    @Query("""
    SELECT SUM(ci.price * ci.quantity)
    FROM CartItem ci
    WHERE ci.cart.id = :cartId
""")
    BigDecimal getTotal(@Param("cartId") Long cartId);

    @Modifying
    @Query("""
    UPDATE CartItem ci
    SET ci.quantity = :quantity
    WHERE ci.id = :id
""")
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
}
