package com.example.demo.repository;

import com.example.demo.models.Voucher;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);
    @Query("""
    SELECT v FROM Voucher v
    WHERE v.active = true
      AND v.startDate <= CURRENT_TIMESTAMP
      AND v.endDate >= CURRENT_TIMESTAMP
      AND v.quantity > v.usedCount
      AND NOT EXISTS (
          SELECT 1 FROM UserVoucher uv
          WHERE uv.user.id = :userId
            AND uv.voucher.id = v.id
      )
    ORDER BY v.value DESC
""")

    List<Voucher> findTopAvailableVouchers(@Param("userId") Long userId, Pageable pageable);
    @Modifying
    @Query("""
    UPDATE Voucher v
    SET v.usedCount = v.usedCount + 1
    WHERE v.code = :code
    AND v.usedCount < v.quantity
""")
    int incrementUsage(@Param("code") String code);
}