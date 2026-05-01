package com.petify.petify.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "clinic_reviews")
public class ClinicReview {

    @Id
    @Column(name = "review_id")
    private Long reviewId;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "review_id")
    private Review review;

    @Column(name = "target_clinic_id", nullable = false)
    private Long targetClinicId;

    public ClinicReview() {
    }

    public ClinicReview(Review review, Long targetClinicId) {
        this.review = review;
        this.targetClinicId = targetClinicId;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public Long getTargetClinicId() {
        return targetClinicId;
    }

    public void setTargetClinicId(Long targetClinicId) {
        this.targetClinicId = targetClinicId;
    }
}
