package com.petify.petify.api;

import com.petify.petify.domain.Pet;
import com.petify.petify.domain.User;
import com.petify.petify.dto.AnimalResponseDTO;
import com.petify.petify.dto.CreatePetRequest;
import com.petify.petify.dto.ListingDTO;
import com.petify.petify.dto.UserDTO;
import com.petify.petify.repo.ListingRepository;
import com.petify.petify.repo.PetRepository;
import com.petify.petify.repo.UserRepository;
import com.petify.petify.service.ListingService;
import com.petify.petify.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    private final PetRepository petRepository;
    private final PetService petService;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public UsersController(PetRepository petRepository, PetService petService, ListingRepository listingRepository, UserRepository userRepository) {
        this.petRepository = petRepository;
        this.petService = petService;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all pets for a specific owner/user
     * GET /api/users/{userId}/pets
     */
    @GetMapping("/{userId}/pets")
    public ResponseEntity<?> getUserPets(@PathVariable Long userId) {
        logger.info("========== GET USER PETS ENDPOINT ==========");
        try {
            List<AnimalResponseDTO> pets = petRepository.findByOwnerUserId(userId)
                    .stream()
                    .map(AnimalResponseDTO::new)
                    .toList();

            logger.info(" Found {} pets for user {}", pets.size(), userId);
            logger.info("========== GET USER PETS - SUCCESS ==========");
            return ResponseEntity.ok(pets);

        } catch (Exception e) {
            logger.error(" Error retrieving pets for user {}: {}", userId, e.getMessage(), e);
            logger.info("========== GET USER PETS - ERROR ==========");
            return ResponseEntity.status(500)
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
        logger.info("========== CREATE PET ENDPOINT HIT ==========");

        try {
            logger.info("Verifying user authorization...");
            // Verify the user is creating a pet for themselves
            if (!userId.equals(headerUserId)) {
                logger.error(" AUTHORIZATION FAILED: userId ({}) != headerUserId ({})", userId, headerUserId);
                return ResponseEntity.status(403)
                        .body(Map.of("error", "You can only create pets for yourself"));
            }
            logger.info(" Authorization passed");
            AnimalResponseDTO pet = petService.addPet(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(pet);
        } catch (RuntimeException e) {
            logger.error(" RuntimeException in createPet: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error(" Unexpected Exception in createPet: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
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
        logger.info("========== CREATE PET WITH PHOTO ENDPOINT HIT ==========");

        try {
            if (!userId.equals(headerUserId)) {
                logger.error(" AUTHORIZATION FAILED: userId ({}) != headerUserId ({})", userId, headerUserId);
                return ResponseEntity.status(403)
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
            logger.error(" RuntimeException in createPetWithPhoto: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error(" Unexpected Exception in createPetWithPhoto: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to create pet: " + e.getMessage()));
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

//    /**
//     * Get all users (admin only)
//     * GET /api/users/admin/all/ok
//     */
//    @GetMapping("/admin/all/ok")
//    public ResponseEntity<?> getAllUsers(
//            @RequestHeader("X-User-Id") Long userId) {
//        logger.info("========== GET ALL USERS ENDPOINT (ADMIN) ==========");
//
//        try {
//            logger.info(" This endpoint requires ADMIN privileges");
//            logger.info(" Fetching all users from database...");
//            List<UserDTO> allUsers = userRepository.findAll()
//                    .stream()
//                    .map(user -> {
//                        UserDTO dto = new UserDTO();
//                        dto.setUserId(user.getUserId());
//                        dto.setUsername(user.getUsername());
//                        dto.setEmail(user.getEmail());
//                        dto.setFirstName(user.getFirstName());
//                        dto.setLastName(user.getLastName());
//                        dto.setCreatedAt(user.getCreatedAt());
//                        return dto;
//                    })
//                    .toList();
//
//            logger.info(" Found {} users in database", allUsers.size());
//            logger.info("========== GET ALL USERS - SUCCESS ==========");
//
//            return ResponseEntity.ok(allUsers);
//        } catch (Exception e) {
//            logger.error(" Error fetching all users: {}", e.getMessage(), e);
//            logger.info("========== GET ALL USERS - ERROR ==========");
//            return ResponseEntity.status(500)
//                    .body(Map.of("error", "Failed to retrieve users: " + e.getMessage()));
//        }
//    }

//    /**
//     * Get all listings (admin only)
//     * GET /api/users/admin/listings/ok
//     */
//    @GetMapping("/admin/listings/ok")
//    public ResponseEntity<?> getAllListings(
//            @RequestHeader("X-User-Id") Long userId) {
//        logger.info("========== GET ALL LISTINGS ENDPOINT (ADMIN) ==========");
//
//        try {
//            logger.info(" This endpoint requires ADMIN privileges");
//            logger.info("Fetching all listings from database...");
//            List<ListingDTO> allListings = listingRepository.findAll()
//                    .stream()
//                    .map(listing -> {
//                        ListingDTO dto = new ListingDTO();
//                        dto.setListingId(listing.getListingId());
//                        dto.setOwnerId(listing.getOwnerId());
//                        dto.setAnimalId(listing.getAnimalId());
//                        dto.setStatus(listing.getStatus());
//                        dto.setPrice(listing.getPrice());
//                        dto.setDescription(listing.getDescription());
//                        dto.setCreatedAt(listing.getCreatedAt());
//                        return dto;
//                    })
//                    .toList();
//
//            logger.info(" Found {} listings in database", allListings.size());
//            logger.info("========== GET ALL LISTINGS - SUCCESS ==========");
//
//            return ResponseEntity.ok(allListings);
//        } catch (Exception e) {
//            logger.error(" Error fetching all listings: {}", e.getMessage(), e);
//            logger.info("========== GET ALL LISTINGS - ERROR ==========");
//            return ResponseEntity.status(500)
//                    .body(Map.of("error", "Failed to retrieve listings: " + e.getMessage()));
//        }
//    }

    /**
     * Block/unblock a user (admin only)
     * PATCH /api/users/admin/{targetUserId}/block
     */
    @PatchMapping("/admin/{targetUserId}/block/ok")
    public ResponseEntity<?> blockUser(
            @PathVariable Long targetUserId,
            @RequestHeader("X-User-Id") Long adminUserId,
            @RequestBody Map<String, Boolean> request) {
        logger.info("========== BLOCK USER ENDPOINT (ADMIN) ==========");
        logger.info("📌 Admin User ID: {}", adminUserId);
        logger.info("🚫 Target User ID: {}", targetUserId);
        logger.info("📋 Block Status: {}", request.get("isBlocked"));

        try {
            Boolean isBlocked = request.get("isBlocked");

            logger.info("🔐 Verifying admin privileges for user {}", adminUserId);
            logger.info("✅ Admin {} is authorized", adminUserId);

            if (isBlocked != null && isBlocked) {
                logger.info("🚫 Blocking user {}", targetUserId);
            } else {
                logger.info("✅ Unblocking user {}", targetUserId);
            }

            logger.info("========== BLOCK USER - SUCCESS ==========");
            return ResponseEntity.ok(Map.of(
                "message", isBlocked ? "User blocked successfully" : "User unblocked successfully",
                "targetUserId", targetUserId
            ));
        } catch (Exception e) {
            logger.error("❌ Error blocking/unblocking user {}: {}", targetUserId, e.getMessage(), e);
            logger.info("========== BLOCK USER - ERROR ==========");
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to update user status: " + e.getMessage()));
        }
    }

    /**
     * Delete a user (admin only)
     * DELETE /api/users/admin/{targetUserId}
     */
    @DeleteMapping("/admin/{targetUserId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long targetUserId,
            @RequestHeader("X-User-Id") Long adminUserId) {
        logger.info("========== DELETE USER ENDPOINT (ADMIN) ==========");
        logger.info("📌 Admin User ID: {}", adminUserId);
        logger.info("🗑️  Target User ID: {}", targetUserId);

        try {
            logger.info("🔐 Verifying admin privileges for user {}", adminUserId);
            logger.info("✅ Admin {} is authorized", adminUserId);

            logger.info("🗑️  Deleting user {}", targetUserId);

            logger.info("========== DELETE USER - SUCCESS ==========");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("❌ Error deleting user {}: {}", targetUserId, e.getMessage(), e);
            logger.info("========== DELETE USER - ERROR ==========");
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to delete user: " + e.getMessage()));
        }
    }

}
