package com.example.demo.repository;

import com.example.demo.Enums.UserRole;
import com.example.demo.Enums.UserStatus;
import com.example.demo.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Query("SELECT COUNT(u) FROM User u")
    Long countTotalUsers();

    @Query("""
            SELECT u FROM User u
            WHERE NOT EXISTS (
                SELECT 1 FROM u.role r
                WHERE r.name = :adminRole
            )
            AND (
                :keyword IS NULL
                OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (
                :status IS NULL
                OR u.status = :status
            )
            """)
    Page<User> findNonAdminUsers(
            @Param("adminRole") UserRole adminRole,
            @Param("keyword") String keyword,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}
