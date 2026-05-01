package com.petify.petify.api;

import com.petify.petify.dto.CreateReviewRequest;
import com.petify.petify.dto.ReviewDTO;
import com.petify.petify.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Create a new review for a user
     * POST /api/reviews/{targetUserId}
     * @param targetUserId the user ID being reviewed
     * @param request the review request
     * @return the created review
     */
    @PostMapping("/{targetUserId}")
    public ResponseEntity<?> createReview(
            @PathVariable Long targetUserId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CreateReviewRequest request) {
        try {
            logger.info("Creating review from user {} for user {}", userId, targetUserId);

            // Validate request
            if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Rating must be between 1 and 5"));
            }

            ReviewDTO review = reviewService.createReview(userId, targetUserId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (RuntimeException e) {
            logger.error("Error creating review: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create review: " + e.getMessage()));
        }
    }

    @PostMapping("/clinics/{clinicId}")
    public ResponseEntity<?> createClinicReview(
            @PathVariable Long clinicId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CreateReviewRequest request) {
        try {
            ReviewDTO review = reviewService.createClinicReview(userId, clinicId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (RuntimeException e) {
            logger.error("Error creating clinic review: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating clinic review: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to create clinic review: " + e.getMessage()));
        }
    }

    @GetMapping("/clinics/{clinicId}")
    public ResponseEntity<?> getReviewsByClinic(@PathVariable Long clinicId) {
        try {
            List<ReviewDTO> reviews = reviewService.getReviewsByClinic(clinicId);
            return ResponseEntity.ok(reviews);
        } catch (RuntimeException e) {
            logger.error("Error fetching clinic reviews: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching clinic reviews: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to fetch clinic reviews: " + e.getMessage()));
        }
    }

    @GetMapping("/clinics/{clinicId}/mine")
    public ResponseEntity<?> getMyClinicReview(
            @PathVariable Long clinicId,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            ReviewDTO review = reviewService.getMyClinicReview(userId, clinicId);
            if (review == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            logger.error("Error fetching my clinic review: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching my clinic review: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to fetch clinic review: " + e.getMessage()));
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CreateReviewRequest request) {
        try {
            ReviewDTO review = reviewService.updateReview(reviewId, userId, request);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            logger.error("Error updating review: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating review: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to update review: " + e.getMessage()));
        }
    }

    /**
     * Get all reviews for a user
     * GET /api/reviews/{targetUserId}
     * @param targetUserId the user ID being reviewed
     * @return list of reviews
     */
    @GetMapping("/{targetUserId}")
    public ResponseEntity<?> getReviewsByUser(@PathVariable Long targetUserId) {
        try {
            logger.info("Fetching reviews for user {}", targetUserId);
            List<ReviewDTO> reviews = reviewService.getReviewsByUser(targetUserId);
            return ResponseEntity.ok(reviews);
        } catch (RuntimeException e) {
            logger.error("Error fetching reviews: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch reviews: " + e.getMessage()));
        }
    }

    /**
     * Delete a review
     * DELETE /api/reviews/{reviewId}
     * @param reviewId the review ID to delete
     * @return success message
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") Long userId) {
        logger.info("=== START DELETE REVIEW ENDPOINT ===");

        try {
            reviewService.deleteReview(reviewId, userId);
            logger.info("=== END DELETE REVIEW ENDPOINT - SUCCESS ===");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error(" RuntimeException occurred: {}", e.getMessage());
            logger.error(" Stack trace:", e);
            logger.info("=== END DELETE REVIEW ENDPOINT - ERROR ===");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error(" Unexpected exception occurred: {}", e.getMessage());
            logger.error(" Stack trace:", e);
            logger.info("=== END DELETE REVIEW ENDPOINT - ERROR ===");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete review: " + e.getMessage()));
        }
    }
}
