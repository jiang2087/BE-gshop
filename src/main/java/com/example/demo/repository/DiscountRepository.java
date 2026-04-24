package com.example.demo.repository;

import com.example.demo.models.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface DiscountRepository extends JpaRepository<Discount,Long> {
    @Query("""
SELECT d FROM Discount d
JOIN d.productVariants p
WHERE p.id = :productId
AND d.active = true
AND :now BETWEEN d.startDate AND d.endDate
""")
    List<Discount> findActiveDiscountByProduct(Long productId, LocalDateTime now);
}
