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
    @MapsId("user_id")
    private User user;

    @ManyToOne
    @MapsId("review_id")
    private Review review;
}
