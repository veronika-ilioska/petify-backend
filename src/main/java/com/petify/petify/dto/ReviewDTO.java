package com.petify.petify.dto;

import com.petify.petify.domain.Review;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ReviewDTO {
    private Long reviewId;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerUsername;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReviewDTO() {
    }

    public ReviewDTO(Review review) {
        this.reviewId = review.getReviewId();
        this.reviewerId = review.getReviewer().getUserId();
        this.reviewerName = review.getReviewer().getFirstName() + " " + review.getReviewer().getLastName();
        this.reviewerUsername = review.getReviewer().getUsername();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
    }

}
