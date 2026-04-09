package com.example.demo.repository;

import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("""
            SELECT new com.example.demo.dto.response.ReviewResponse(
                r.id,
                r.user.username,
                r.comment,
                r.createdAt,
                r.rating,
                r.avatar,
                r.order.id,
                r.user.id,
                pv.product.id,
                r.updatedAt,
                r.helpfulCount
            )
            FROM Review r
            JOIN r.productVariant pv
            WHERE pv.product.id = :productId
            """)
    List<ReviewResponse> findByProductId(Long productId);

    @Modifying
    @Query("""
                UPDATE Review r
                SET r.helpfulCount = r.helpfulCount + 1
                WHERE r.id = :reviewId
            """)
    int increaseHelpfulCount(Long reviewId);

    @Modifying
    @Query("""
                UPDATE Review r
                SET r.helpfulCount = r.helpfulCount - 1
                WHERE r.id = :reviewId
            """)
    int decreaseHelpfulCount(Long reviewId);
}
