package com.example.demo.repository;

import com.example.demo.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
                SELECT o.id
                FROM Order o
                WHERE o.user.id = :id
            """)
    Optional<Long> findFirstByUserId(@Param("id") Long id);

    @Query("""
                SELECT o.id
                FROM Order o
                JOIN o.items oi
                JOIN oi.productVariant pv
                WHERE o.user.id = :userId
                AND pv.id = :productId
            """)
    Long findByUserIdAndProductId(@Param("userId") Long userId,
                                       @Param("productId") Long productId);
}
