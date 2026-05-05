package com.petify.petify.api;

import com.petify.petify.domain.Client;
import com.petify.petify.dto.AdminListingsPageDTO;
import com.petify.petify.dto.AnimalResponseDTO;
import com.petify.petify.dto.CreatePetRequest;
import com.petify.petify.dto.ListingDTO;
import com.petify.petify.dto.UserDTO;
import com.petify.petify.dto.UserActivityRankingProjection;
import com.petify.petify.repo.AdminRepository;
import com.petify.petify.repo.AnalyticsRepository;
import com.petify.petify.repo.ClientRepository;
import com.petify.petify.repo.PetRepository;
import com.petify.petify.service.AuthService;
import com.petify.petify.service.ListingService;
import com.petify.petify.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final AdminRepository adminRepository;
    private final PetRepository petRepository;
    private final PetService petService;

    public UserManagementController(AuthService authService,
                                    AnalyticsRepository analyticsRepository,
                                    ListingService listingService,
                                    ClientRepository clientRepository,
                                    AdminRepository adminRepository,
                                    PetRepository petRepository,
                                    PetService petService) {
        this.authService = authService;
        this.analyticsRepository = analyticsRepository;
        this.listingService = listingService;
        this.clientRepository = clientRepository;
        this.adminRepository = adminRepository;
        this.petRepository = petRepository;
        this.petService = petService;
    }

    private boolean isAdmin(Long userId) {
        return userId != null && adminRepository.existsById(userId);
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
            logger.error(" Error in getAllUsers: {}", e.getMessage(), e);
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
     * Get all pets for a specific owner/user
     * GET /api/users/{userId}/pets
     */
    @GetMapping("/{userId}/pets")
    public ResponseEntity<?> getUserPets(@PathVariable Long userId) {
        try {
            List<AnimalResponseDTO> pets = petRepository.findByOwnerUserId(userId)
                    .stream()
                    .map(AnimalResponseDTO::new)
                    .toList();

            logger.info("Found {} pets for user {}", pets.size(), userId);
            return ResponseEntity.ok(pets);
        } catch (Exception e) {
            logger.error("Error retrieving pets for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve pets: " + e.getMessage()));
        }
    }

    /**
     * Add a new pet for a user (promotes CLIENT to OWNER if needed)
     * POST /api/users/{userId}/pets
     */
    @PostMapping(value = "/{userId}/pets", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPet(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long headerUserId,
            @RequestBody CreatePetRequest request) {
        try {
            if (!userId.equals(headerUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only create pets for yourself"));
            }

            AnimalResponseDTO pet = petService.addPet(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(pet);
        } catch (RuntimeException e) {
            logger.error("RuntimeException in createPet: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected Exception in createPet: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create pet: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/{userId}/pets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPetWithPhoto(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long headerUserId,
            @RequestParam String name,
            @RequestParam String sex,
            @RequestParam String type,
            @RequestParam String species,
            @RequestParam(required = false) String dateOfBirth,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) String locatedName,
            @RequestPart(required = false) MultipartFile photo) {
        try {
            if (!userId.equals(headerUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only create pets for yourself"));
            }

            CreatePetRequest request = new CreatePetRequest();
            request.setName(name);
            request.setSex(sex);
            request.setType(type);
            request.setSpecies(species);
            request.setBreed(blankToNull(breed));
            request.setLocatedName(blankToNull(locatedName));
            if (dateOfBirth != null && !dateOfBirth.isBlank()) {
                request.setDateOfBirth(LocalDate.parse(dateOfBirth));
            }

            AnimalResponseDTO pet = petService.addPet(userId, request, photo);
            return ResponseEntity.status(HttpStatus.CREATED).body(pet);
        } catch (RuntimeException e) {
            logger.error("RuntimeException in createPetWithPhoto: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected Exception in createPetWithPhoto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create pet: " + e.getMessage()));
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
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

            if (!isAdmin(userId)) {
                logger.warn(" Forbidden: user {} attempted to access admin users endpoint", userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            List<UserDTO> users = authService.getAllUsers();
            logger.info("Retrieved {} users from AuthService", users.size());

            for (int i = 0; i < Math.min(users.size(), 3); i++) {
                UserDTO user = users.get(i);
                logger.info("User {}: ID={}, Username={}, UserType={}",
                        i, user.getUserId(), user.getUsername(), user.getUserType());
            }

            logger.info("========== RETURNING USERS ==========");
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error(" Error fetching users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Get paged listings (Admin only)
     * GET /api/users/admin/listings?page=0&size=500&status=ACTIVE
     */
    @GetMapping("/admin/listings")
    public ResponseEntity<?> getAllListingsAdmin(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        try {
            logger.info("========== GET ALL LISTINGS (ADMIN) ==========");
            logger.info("Admin User ID: {}", userId);

            if (!isAdmin(userId)) {
                logger.warn(" Forbidden: user {} attempted to access admin listings endpoint", userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Admin access required"));
            }
            int safePage = Math.max(page, 0);
            int safeSize = Math.min(Math.max(size, 1), 500);
            String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
            PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

            Page<ListingDTO> listings = listingService.getAdminListings(
                    normalizedStatus,
                    minPrice,
                    maxPrice,
                    pageRequest
            );
            logger.info("Retrieved listing page {} with {} rows for status '{}', minPrice {}, maxPrice {}",
                    safePage,
                    listings.getNumberOfElements(),
                    normalizedStatus.isBlank() ? "ALL" : normalizedStatus,
                    minPrice,
                    maxPrice);

            return ResponseEntity.ok(new AdminListingsPageDTO(
                    listings.getContent(),
                    listings.getNumber(),
                    listings.getSize(),
                    listings.getTotalElements(),
                    listings.getTotalPages(),
                    listings.hasNext(),
                    listings.hasPrevious(),
                    listingService.countListingsByStatus("ACTIVE"),
                    listingService.countListingsByStatus("SOLD")
            ));
        } catch (Exception e) {
            logger.error("Error fetching listings: {}", e.getMessage(), e);
            Map<String, Object> errorBody = new java.util.HashMap<>();
            errorBody.put("error", "Failed to fetch listings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody);
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
            boolean verified = authService.isUserInTopActive(userId);
            return ResponseEntity.ok(Map.of("userId", userId, "verified", verified));
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

            if (!isAdmin(adminUserId)) {
                logger.warn(" Forbidden: user {} attempted to block/unblock user {}", adminUserId, targetUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Admin access required"));
            }

            boolean isBlocked = (Boolean) request.getOrDefault("isBlocked", false);
            String blockedReason = (String) request.getOrDefault("blockedReason", "");

            Client client = clientRepository.findById(targetUserId)
                    .orElseThrow(() -> new RuntimeException("Client not found"));

            if (isBlocked) {
                logger.info(" Blocking user {} with reason: {}", targetUserId, blockedReason);
                client.setBlocked(true);
                client.setBlockedReason(blockedReason);
                client.setBlockedAt(java.time.LocalDateTime.now());
            } else {
                logger.info(" Unblocking user {}", targetUserId);
                client.setBlocked(false);
                client.setBlockedReason("");
                client.setBlockedAt(null);
            }

            clientRepository.save(client);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", isBlocked ? "User blocked successfully" : "User unblocked successfully",
                    "userId", targetUserId,
                    "isBlocked", isBlocked
            ));
        } catch (Exception e) {
            logger.error(" Error blocking/unblocking user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to block/unblock user: " + e.getMessage()));
        }
    }
}
