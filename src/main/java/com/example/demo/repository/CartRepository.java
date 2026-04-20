package com.example.demo.repository;

import com.example.demo.Enums.CartStatus;
import com.example.demo.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCartKey(String cartKey);
    Optional<Cart> findByUserId(Long userId);
    Optional<Cart> findByUserIdAndStatus(Long userId,  CartStatus status);

}
