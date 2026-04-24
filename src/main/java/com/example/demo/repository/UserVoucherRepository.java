package com.example.demo.repository;

import com.example.demo.models.User;
import com.example.demo.models.Voucher;
import com.example.demo.models.junction.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    boolean existsByUserAndVoucher(User user, Voucher voucher);

    @Query("SELECT uv FROM UserVoucher uv WHERE uv.user.id = :userId AND uv.voucher.code = :code")
    Optional<UserVoucher> findByUserIdAndVoucherCode(Long userId, String code);
}