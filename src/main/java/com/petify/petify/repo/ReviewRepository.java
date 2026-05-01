package com.petify.petify.repo;

import com.petify.petify.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByReviewerUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long reviewerId);
}
