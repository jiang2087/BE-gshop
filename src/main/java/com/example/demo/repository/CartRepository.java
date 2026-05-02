package com.example.demo.repository;

import com.example.demo.Enums.CartStatus;
import com.example.demo.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCartKey(String cartKey);

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

    @Query("""
                   DELETE FROM CartItem ci
                    WHERE ci.cart.id IN (
                        SELECT c.id FROM Cart c WHERE c.user.id = :userId
                    )
            """)
    @Modifying
    void deleteByUserId(Long userId);

}
