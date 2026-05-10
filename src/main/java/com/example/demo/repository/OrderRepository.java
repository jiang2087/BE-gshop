package com.example.demo.repository;

import com.example.demo.Enums.OrderStatus;
import com.example.demo.dto.response.UserPurchaserProjection;
import com.example.demo.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByOrderCodeContainingIgnoreCase(String orderCode, Pageable pageable);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Order> findByIdAndUserId(Long id, Long userId);

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
                ORDER BY o.createdAt DESC
            """)
    List<Long> findByUserIdAndProductId(@Param("userId") Long userId,
                                        @Param("productId") Long productId);

    @Query("""
    SELECT COALESCE(SUM(o.totalPrice), 0)
    FROM Order o
    WHERE o.status = 'COMPLETED'
      AND o.createdAt >= :startOfMonth
      AND o.createdAt < :startOfNextMonth
""")
    BigDecimal getTotalProfitThisMonth(
            @Param("startOfMonth") LocalDateTime startOfMonth,
            @Param("startOfNextMonth") LocalDateTime startOfNextMonth
    );

    @Query("""
  SELECT FUNCTION('dayofweek', o.createdAt), COALESCE(SUM(o.totalPrice), 0)
  FROM Order o
  WHERE o.status = 'COMPLETED'
    AND FUNCTION('week', o.createdAt) = FUNCTION('week', :date)
    AND FUNCTION('year', o.createdAt) = FUNCTION('year', :date)
  GROUP BY FUNCTION('dayofweek', o.createdAt)
  ORDER BY FUNCTION('dayofweek', o.createdAt)
""")
    List<Object[]> getRevenueByDayOfWeek(@Param("date") LocalDate date);

    @Query("""
    SELECT MONTH(o.createdAt), COALESCE(SUM(o.totalPrice), 0)
    FROM Order o
    WHERE o.status = 'COMPLETED'
      AND YEAR(o.createdAt) = :year
    GROUP BY MONTH(o.createdAt)
    ORDER BY MONTH(o.createdAt)
""")
    List<Object[]> getProfitPerMonth(@Param("year") int year);

    @Query("""
  SELECT FUNCTION('week', o.createdAt), COALESCE(SUM(o.totalPrice), 0)
  FROM Order o
  WHERE o.status = 'COMPLETED'
    AND YEAR(o.createdAt) = :year
  GROUP BY FUNCTION('week', o.createdAt)
  ORDER BY FUNCTION('week', o.createdAt)
""")
    List<Object[]> getProfitPerWeek(@Param("year") int year);

    @Query(
            value = """
    SELECT u.id AS userId,
           u.username AS username,
           u.email AS email,
           COALESCE(SUM(o.totalPrice), 0) AS totalPurchased,
           MAX(o.createdAt) AS lastPurchase
    FROM Order o
    JOIN o.user u
    WHERE o.status = 'COMPLETED'
    GROUP BY u.id, u.username, u.email
    ORDER BY COALESCE(SUM(o.totalPrice), 0) DESC
""",
            countQuery = """
    SELECT COUNT(DISTINCT u.id)
    FROM Order o
    JOIN o.user u
    WHERE o.status = 'COMPLETED'
"""
    )
    Page<UserPurchaserProjection> findUserPurchaseTotalsDesc(Pageable pageable);

}
