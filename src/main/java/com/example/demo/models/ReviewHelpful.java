package com.example.demo.models;

import com.example.demo.models.junction.ReviewHelpfulId;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "review_helpful")
public class ReviewHelpful {
    @EmbeddedId
    private ReviewHelpfulId id;

    @ManyToOne
    @MapsId("userId")
    private User user;

    @ManyToOne
    @MapsId("reviewId")
    private Review review;
}
