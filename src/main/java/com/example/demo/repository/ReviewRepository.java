package com.example.demo.repository;

import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.dto.response.ReviewStats;
import com.example.demo.models.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
            ORDER BY r.helpfulCount desc
            """)
    List<ReviewResponse> findByProductId(Long productId);

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
                ORDER BY r.rating DESC, r.helpfulCount DESC
            """)
    List<ReviewResponse> findTopReviews(Pageable pageable);

    @Modifying
    @Query("""
                UPDATE Review r
                SET r.helpfulCount = r.helpfulCount + 1
                WHERE r.id = :reviewId
            """)
    void increaseHelpfulCount(Long reviewId);

    @Modifying
    @Query("""
                UPDATE Review r
                SET r.helpfulCount = r.helpfulCount - 1
                WHERE r.id = :reviewId
            """)
    void decreaseHelpfulCount(Long reviewId);

    @Query("""
            SELECT new com.example.demo.dto.response.ReviewStats(
                   pv.product.id,
                   COUNT(r.id),
                   COALESCE(AVG(r.rating), 0)
               )
               FROM Review r
               JOIN r.productVariant pv
               WHERE pv.product.id IN :ids
               GROUP BY pv.product.id
            """)
    List<ReviewStats> getReviewStats(@Param("ids") List<Long> ids);
}
