package com.example.demo.services;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.models.Review;
import com.example.demo.models.ReviewHelpful;
import com.example.demo.models.junction.ReviewHelpfulId;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ReviewHelpfulRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.products.ProductRepository;
import com.example.demo.repository.products.ProductVariantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewHelpfulRepository reviewHelpfulRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    public List<ReviewResponse> getByProductId(Long id) {
        return reviewRepository.findByProductId(id);
    }

    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        Long orderId = orderRepository.findByUserIdAndProductId(request.userId(), request.productVariantId());
        if (orderId != null) {
            Review review = new Review();
            review.setAvatar(request.avatar());
            review.setProductVariant(productVariantRepository.getReferenceById(request.productVariantId()));
            review.setOrder(orderRepository.getReferenceById(orderId));
            review.setComment(request.comment());
            review.setHelpfulCount(0);
            review.setUser(userRepository.getReferenceById(request.userId()));
            review.setRating(request.rating());
            reviewRepository.save(review);
            return new ReviewResponse(
                    review.getId(),
                    review.getUser().getUsername(),
                    review.getComment(),
                    review.getCreatedAt(),
                    review.getRating(),
                    review.getAvatar(),
                    review.getOrder().getId(),
                    review.getUser().getId(),
                    request.productVariantId(),
                    null,
                    0
            );
        }else{
            throw new RuntimeException("you has not purchased this product yet.");
        }
    }

    @Transactional
    public ReviewResponse updateReview(Long userId,Long reviewId ,ReviewRequest request) {
         if(Objects.equals(userId, request.userId())){
            Review review = reviewRepository.findById(reviewId).orElseThrow(
                    () -> new RuntimeException("you can not adjust this review.")
            );
            review.setComment(request.comment());
            review.setRating(request.rating());
            reviewRepository.save(review);
             return new ReviewResponse(
                     review.getId(),
                     review.getUser().getUsername(),
                     review.getComment(),
                     review.getCreatedAt(),
                     review.getRating(),
                     review.getAvatar(),
                     review.getOrder().getId(),
                     review.getUser().getId(),
                     request.productVariantId(),
                     review.getUpdatedAt(),
                     review.getHelpfulCount()
             );
         } else {
             throw new RuntimeException("you can not adjust this Comment.");
         }
    }
    @Transactional
    public void toggleHelpful(Long userId, Long reviewId) {

        boolean exists = reviewHelpfulRepository
                .existsByUserIdAndReviewId(userId, reviewId);

        if (exists) {
            reviewHelpfulRepository.deleteByUserIdAndReviewId(userId, reviewId);
            reviewRepository.decreaseHelpfulCount(reviewId);
        } else {
            ReviewHelpful helpful = new ReviewHelpful();
            helpful.setId(new ReviewHelpfulId(userId, reviewId));
            helpful.setUser(userRepository.getReferenceById(userId));
            helpful.setReview(reviewRepository.getReferenceById(reviewId));

            reviewHelpfulRepository.save(helpful);
            reviewRepository.increaseHelpfulCount(reviewId);
        }
    }
}
