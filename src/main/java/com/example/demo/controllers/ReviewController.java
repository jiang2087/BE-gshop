package com.example.demo.controllers;


import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getByProductId(id));
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest review) {
        return ResponseEntity.ok(reviewService.createReview(review));
    }

    @PostMapping("/helpful")
    public ResponseEntity<?> toggleReviewHelpful(@RequestParam Long reviewId, @RequestParam Long userId) {
        boolean isHelpful = reviewService.toggleHelpful(userId, reviewId);
        return ResponseEntity.ok(isHelpful);
    }

    @PutMapping
    public ResponseEntity<?> updateReview(@RequestParam Long reviewId,
                                          @RequestParam Long userId,
                                          @RequestBody ReviewRequest reviewRequest) {
        return ResponseEntity.ok(reviewService.updateReview(userId, reviewId, reviewRequest));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

}
