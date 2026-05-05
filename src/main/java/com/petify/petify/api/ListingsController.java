package com.petify.petify.api;

import com.petify.petify.dto.CreateListingRequest;
import com.petify.petify.dto.ListingDTO;
import com.petify.petify.service.ListingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/listings")
public class ListingsController {

    private static final Logger logger = LoggerFactory.getLogger(ListingsController.class);
    private final ListingService listingService;

    public ListingsController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping
    public ResponseEntity<?> createListing(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CreateListingRequest request) {
        try {
            if (request.getAnimalId() == null || request.getPrice() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "animalId and price are required"));
            }

            ListingDTO listing = listingService.createListing(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(listing);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create listing: " + e.getMessage()));
        }
    }

    @GetMapping("/my-listings")
    public ResponseEntity<?> getMyListings(@RequestHeader("X-User-Id") Long userId) {
        try {
            List<ListingDTO> listings = listingService.getListingsByOwner(userId);
            return ResponseEntity.ok(listings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve listings: " + e.getMessage()));
        }
    }


    @GetMapping("/{listingId}")
    public ResponseEntity<?> getListingById(@PathVariable Long listingId) {
        try {
            ListingDTO listing = listingService.getListingById(listingId);
            return ResponseEntity.ok(listing);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update listing status (only owner can update)
     * PATCH /api/listings/{listingId}/status
     */
    @PatchMapping("/{listingId}/status")
    public ResponseEntity<?> updateListingStatus(
            @PathVariable Long listingId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> request) {

        try {
            String status = request.get("status");
            logger.info(" New Status requested: {}", status);

            if (status == null || status.isEmpty()) {
                logger.error(" Status is null or empty");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "status is required"));
            }

            ListingDTO listing = listingService.updateListingStatus(listingId, status, userId);

            logger.info("===  SUCCESS ===");
            return ResponseEntity.ok(listing);
        } catch (RuntimeException e) {
            logger.error(" RuntimeException occurred: {}", e.getMessage());
            logger.error(" Stack trace:", e);
            logger.info("===  ERROR ===");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error(" Unexpected exception occurred: {}", e.getMessage());
            logger.error(" Stack trace:", e);
            logger.info("===  ERROR ===");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update listing status: " + e.getMessage()));
        }
    }

    /**
     * Delete a listing (only owner can delete)
     * DELETE /api/listings/{listingId}
     */
    @DeleteMapping("/{listingId}")
    public ResponseEntity<?> deleteListing(
            @PathVariable Long listingId,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            listingService.deleteListing(listingId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all active listings
     * GET /api/listings/active
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveListings() {
        try {
            List<ListingDTO> listings = listingService.getActiveListings();
            return ResponseEntity.ok(listings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve active listings: " + e.getMessage()));
        }
    }

    /**
     * Get recommended listings for a user
     * GET /api/listings/recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendedListings(@RequestHeader("X-User-Id") Long userId) {
        try {
            logger.info("Fetching recommended listings for user ID: {}", userId);
            List<ListingDTO> listings = listingService.getRecommendedListings(userId);
            logger.info("Retrieved {} recommended listings", listings.size());
            return ResponseEntity.ok(listings);
        } catch (Exception e) {
            logger.error("Failed to retrieve recommended listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve recommended listings: " + e.getMessage()));
        }
    }
}
