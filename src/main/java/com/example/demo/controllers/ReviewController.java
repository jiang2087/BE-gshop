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
    public ResponseEntity<?> getReview(@PathVariable long id) {
        return ResponseEntity.ok(reviewService.getByProductId(id));
    }
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest review) {
        return ResponseEntity.ok(reviewService.createReview(review));
    }
    @PostMapping("/helpful")
    public ResponseEntity<?> toggleReviewHelpful(@RequestParam long reviewId, @RequestParam long userId) {
        reviewService.toggleHelpful(userId, reviewId);
        return ResponseEntity.ok("Review has been successfully toggled.");
    }
}
