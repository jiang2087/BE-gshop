package com.example.demo.repository.junction;

import com.example.demo.models.ReviewHelpful;
import com.example.demo.models.junction.ReviewHelpfulId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewHelpfulRepository extends JpaRepository<ReviewHelpful, ReviewHelpfulId> {

    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);

    void deleteByUserIdAndReviewId(Long userId, Long reviewId);

    void deleteByReviewId(Long reviewId);
}
