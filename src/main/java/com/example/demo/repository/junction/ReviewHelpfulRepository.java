package com.example.demo.repository.junction;

import com.example.demo.models.ReviewHelpful;
import com.example.demo.models.junction.ReviewHelpfulId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewHelpfulRepository extends JpaRepository<ReviewHelpful, ReviewHelpfulId> {

    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);

    @Query("""
    SELECT r.id
    FROM ReviewHelpful rh
    JOIN rh.review r
    WHERE rh.user.id = :userId
      AND r.productVariant.product.id = :productVariantId
""")
    List<Long> findLikedReviewsByUserAndProductVariant(
            Long userId,
            Long productVariantId
    );

    void deleteByUserIdAndReviewId(Long userId, Long reviewId);

    void deleteByReviewId(Long reviewId);
}
