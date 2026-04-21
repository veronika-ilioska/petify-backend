package com.petify.petify.api;

import com.petify.petify.domain.Client;
import com.petify.petify.dto.ListingDTO;
import com.petify.petify.dto.UserDTO;
import com.petify.petify.dto.UserActivityRankingProjection;
import com.petify.petify.repo.AnalyticsRepository;
import com.petify.petify.repo.ClientRepository;
import com.petify.petify.service.AuthService;
import com.petify.petify.service.ListingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    private final AuthService authService;
    private final AnalyticsRepository analyticsRepository;
    private final ListingService listingService;
    private final ClientRepository clientRepository;

    public UserManagementController(AuthService authService, AnalyticsRepository analyticsRepository, ListingService listingService, ClientRepository clientRepository) {
        this.authService = authService;
        this.analyticsRepository = analyticsRepository;
        this.listingService = listingService;
        this.clientRepository = clientRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("===== GET ALL USERS (Public) =====");
        try {
            List<UserDTO> users = authService.getAllUsers();
            logger.info("✓ Retrieved {} users", users.size());
            for (int i = 0; i < Math.min(users.size(), 3); i++) {
                UserDTO user = users.get(i);
                logger.info("User {}: ID={}, Type={}", i, user.getUserId(), user.getUserType());
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("❌ Error in getAllUsers: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get user by ID
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        try {
            UserDTO user = authService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get user by username
     * GET /api/users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        try {
            UserDTO user = authService.getUserByUsername(username);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all users (Admin only)
     * GET /api/users/admin/all
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<UserDTO>> getAllUsersAdmin(@RequestHeader("X-User-Id") Long userId) {
        try {
            logger.info("========== GET ALL USERS (ADMIN) ==========");
            logger.info("Admin User ID: {}", userId);

            List<UserDTO> users = authService.getAllUsers();
            logger.info("✓ Retrieved {} users from AuthService", users.size());

            // Log first few users to verify userType is included
            for (int i = 0; i < Math.min(users.size(), 3); i++) {
                UserDTO user = users.get(i);
                logger.info("User {}: ID={}, Username={}, UserType={}",
                        i, user.getUserId(), user.getUsername(), user.getUserType());
            }

            logger.info("========== RETURNING {} USERS ==========", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("❌ Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Get all listings (Admin only)
     * GET /api/users/admin/listings
     */
    @GetMapping("/admin/listings")
    public ResponseEntity<List<Map<String, Object>>> getAllListingsAdmin(@RequestHeader("X-User-Id") Long userId) {
        try {
            logger.info("========== GET ALL LISTINGS (ADMIN) ==========");
            logger.info("Admin User ID: {}", userId);

            List<ListingDTO> listings = listingService.getAllListings();
            logger.info("✓ Retrieved {} listings from ListingService", listings.size());

            // Enrich listings with owner names
            List<Map<String, Object>> enrichedListings = listings.stream()
                    .map(listing -> {
                        Map<String, Object> map = new java.util.HashMap<>();
                        map.put("listingId", listing.getListingId());
                        map.put("animalId", listing.getAnimalId());
                        map.put("ownerId", listing.getOwnerId());
                        map.put("price", listing.getPrice());
                        map.put("status", listing.getStatus());
                        map.put("description", listing.getDescription());
                        map.put("createdAt", listing.getCreatedAt());

                        // Fetch owner name from users table
                        if (listing.getOwnerId() != null) {
                            try {
                                UserDTO owner = authService.getUserById(listing.getOwnerId());
                                map.put("ownerName", owner.getFirstName() + " " + owner.getLastName());
                                map.put("ownerUsername", owner.getUsername());
                                logger.debug("✓ Owner for listing {}: {}", listing.getListingId(), map.get("ownerName"));
                            } catch (Exception e) {
                                logger.warn("⚠ Could not fetch owner for listing {}: {}", listing.getListingId(), e.getMessage());
                                map.put("ownerName", "Unknown");
                                map.put("ownerUsername", "Unknown");
                            }
                        } else {
                            map.put("ownerName", "Unknown");
                            map.put("ownerUsername", "Unknown");
                        }

                        return map;
                    })
                    .collect(java.util.stream.Collectors.toList());

            logger.info("✓ Enriched {} listings with owner information", enrichedListings.size());
            logger.info("========== RETURNING {} LISTINGS ==========", enrichedListings.size());
            return ResponseEntity.ok(enrichedListings);
        } catch (Exception e) {
            logger.error("❌ Error fetching listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Get top 10 active users for verification
     * GET /api/users/verification/top-10
     */
    @GetMapping("/verification/top-10")
    public ResponseEntity<?> getTop10VerifiedUsers() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime thirtyDaysAgo = now.minusDays(30);
            List<UserActivityRankingProjection> topUsers = analyticsRepository.getTopActiveUsers(thirtyDaysAgo, now);
            List<Long> topUserIds = topUsers.stream().map(UserActivityRankingProjection::getUserId).toList();
            return ResponseEntity.ok(Map.of("topUsers", topUserIds, "count", topUserIds.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch top 10 users: " + e.getMessage()));
        }
    }

    /**
     * Check if user is verified (in top 10)
     * GET /api/users/{userId}/verified
     */
    @GetMapping("/{userId}/verified")
    public ResponseEntity<?> isUserVerified(@PathVariable Long userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime thirtyDaysAgo = now.minusDays(30);
            List<UserActivityRankingProjection> topUsers = analyticsRepository.getTopActiveUsers(thirtyDaysAgo, now);
            boolean isVerified = topUsers.stream().anyMatch(u -> u.getUserId().equals(userId));
            return ResponseEntity.ok(Map.of("userId", userId, "verified", isVerified));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check verification status: " + e.getMessage()));
        }
    }

    /**
     * Block/Unblock user (Admin only)
     * PATCH /api/users/admin/{userId}/block
     */
    @PatchMapping("/admin/{targetUserId}/block")
    public ResponseEntity<?> blockUser(
            @PathVariable Long targetUserId,
            @RequestHeader("X-User-Id") Long adminUserId,
            @RequestBody Map<String, Object> request) {
        try {
            logger.info("========== BLOCK/UNBLOCK USER (ADMIN) ==========");
            logger.info("Admin ID: {}, Target User ID: {}", adminUserId, targetUserId);

            boolean isBlocked = (Boolean) request.getOrDefault("isBlocked", false);
            String blockedReason = (String) request.getOrDefault("blockedReason", "");

            // Get the client to update
            Client client = clientRepository.findById(targetUserId)
                    .orElseThrow(() -> new RuntimeException("Client not found"));

            if (isBlocked) {
                logger.info("✓ Blocking user {} with reason: {}", targetUserId, blockedReason);
                client.setBlocked(true);
                client.setBlockedReason(blockedReason);
                client.setBlockedAt(java.time.LocalDateTime.now());
            } else {
                logger.info("✓ Unblocking user {}", targetUserId);
                client.setBlocked(false);
                client.setBlockedReason("");
                client.setBlockedAt(null);
            }

            clientRepository.save(client);
            logger.info("========== USER {} SUCCESSFULLY {}", targetUserId, isBlocked ? "BLOCKED" : "UNBLOCKED");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", isBlocked ? "User blocked successfully" : "User unblocked successfully",
                    "userId", targetUserId,
                    "isBlocked", isBlocked
            ));
        } catch (Exception e) {
            logger.error("❌ Error blocking/unblocking user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to block/unblock user: " + e.getMessage()));
        }
    }
}
