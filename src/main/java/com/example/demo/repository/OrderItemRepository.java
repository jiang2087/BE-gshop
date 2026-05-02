package com.example.demo.repository;


import com.example.demo.models.products.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByProductVariantId(Long variantId);

    @Query("""
                SELECT oi FROM OrderItem oi
                JOIN oi.order o
                WHERE o.user.id = :userId
            """)
    List<OrderItem> findByUserId(@Param("userId") Long userId);

    @Query("""
                SELECT oi FROM OrderItem oi
                JOIN oi.productVariant pv
                JOIN pv.product p
                WHERE p.id = :productId
            """)
    List<OrderItem> findByProductId(@Param("productId") Long productId);

    void deleteByOrderId(Long orderId);
}
