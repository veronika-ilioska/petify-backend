package com.petify.petify.repo;

import com.petify.petify.domain.ClinicReview;
import com.petify.petify.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClinicReviewRepository extends JpaRepository<ClinicReview, Long> {

    Optional<ClinicReview> findTopByReviewReviewerUserIdAndTargetClinicIdAndReviewIsDeletedFalseOrderByReviewCreatedAtDesc(
        Long reviewerId,
        Long targetClinicId
    );

    @Query("""
        select r
        from ClinicReview cr
        join cr.review r
        join fetch r.reviewer
        where cr.targetClinicId = :clinicId
        and r.isDeleted = false
        order by r.createdAt desc
    """)
    List<Review> findReviewsForClinic(@Param("clinicId") Long clinicId);
}
