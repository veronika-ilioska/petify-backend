package com.petify.petify.service;

import com.petify.petify.domain.Listing;
import com.petify.petify.domain.Owner;
import com.petify.petify.dto.CreateListingRequest;
import com.petify.petify.dto.ListingDTO;
import com.petify.petify.repo.ListingRepository;
import com.petify.petify.repo.OwnerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListingService {

    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);

    private final ListingRepository listingRepository;
    private final OwnerRepository ownerRepository;

    public ListingService(ListingRepository listingRepository, OwnerRepository ownerRepository) {
        this.listingRepository = listingRepository;
        this.ownerRepository = ownerRepository;
    }

    /**
     * Create a new listing (only owners can create listings)
     */
    @Transactional
    public ListingDTO createListing(Long userId, CreateListingRequest request) {
        // Check if user is an owner
        Owner owner = ownerRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("User is not an owner. Only owners can create listings."));

        logger.info("Creating listing for owner ID: {}", userId);

        // Create new listing with Owner object
        Listing listing = new Listing(
            owner,
            request.getAnimalId(),
            request.getPrice(),
            request.getDescription()
        );

        Listing savedListing = listingRepository.save(listing);

        logger.info("Listing created successfully - ID: {}, Owner ID: {}, Animal ID: {}",
            savedListing.getListingId(), userId, request.getAnimalId());

        return mapToDTO(savedListing);
    }

    /**
     * Get all listings for a specific owner
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getListingsByOwner(Long userId) {
        Owner owner = ownerRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Owner not found"));

        return listingRepository.findByOwner(owner)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get all active listings
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getActiveListings() {
        return listingRepository.findByStatus("ACTIVE")
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get all listings (for admin)
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getAllListings() {
        return listingRepository.findAll()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ListingDTO> getAllListings(Pageable pageable) {
        return listingRepository.findAll(pageable)
            .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ListingDTO> getListingsByStatus(String status, Pageable pageable) {
        return listingRepository.findByStatusIgnoreCase(status, pageable)
            .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ListingDTO> getAdminListings(String status, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim();
        boolean hasMinPrice = minPrice != null;
        boolean hasMaxPrice = maxPrice != null;

        if (normalizedStatus == null && !hasMinPrice && !hasMaxPrice) {
            return listingRepository.findAll(pageable)
                .map(this::mapToDTO);
        }

        if (normalizedStatus != null && !hasMinPrice && !hasMaxPrice) {
            return listingRepository.findByStatusIgnoreCase(normalizedStatus, pageable)
                .map(this::mapToDTO);
        }

        return listingRepository.findAdminListings(normalizedStatus, minPrice, maxPrice, pageable)
            .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public long countListingsByStatus(String status) {
        return listingRepository.countByStatus(status);
    }

    /**
     * Get recommended listings for a user based on their favorites
     * Uses collaborative filtering and content-based filtering
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getRecommendedListings(Long userId) {
        logger.info("Fetching recommended listings for user ID: {}", userId);

        try {
            List<Object[]> results = listingRepository.findRecommendedListings(userId);
            logger.info("Found {} recommended listings", results.size());

            return results.stream()
                .map(this::mapRecommendationToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching recommended listings: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Map recommendation query result to ListingDTO
     */
    private ListingDTO mapRecommendationToDTO(Object[] result) {
        // Result columns: listing_id, title, species, breed, location, created_at, cf_score, liked_by_similar_users, content_score, final_score
        Long listingId = ((Number) result[0]).longValue();

        // Get the actual listing to map to DTO properly
        return listingRepository.findById(listingId)
            .map(this::mapToDTO)
            .orElse(null);
    }

    /**
     * Get a specific listing by ID
     */
    @Transactional(readOnly = true)
    public ListingDTO getListingById(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new RuntimeException("Listing not found"));
        return mapToDTO(listing);
    }

    /**
     * Update listing status
     */
    @Transactional
    public ListingDTO updateListingStatus(Long listingId, String status, Long userId) {
        logger.info("=== START UPDATE LISTING STATUS ===");

        logger.info(" Fetching listing with ID: {}", listingId);
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> {
                logger.error(" Listing not found with ID: {}", listingId);
                return new RuntimeException("Listing not found");
            });
        logger.info(" Listing found - Owner ID: {}, Current Status: {}", listing.getOwnerId(), listing.getStatus());

        // Verify owner
        logger.info(" Verifying ownership - Listing Owner: {}, Requesting User: {}", listing.getOwnerId(), userId);
        if (!listing.getOwnerId().equals(userId)) {
            logger.error(" User {} is not authorized to update listing {}. Owner is {}", userId, listingId, listing.getOwnerId());
            throw new RuntimeException("You can only update your own listings");
        }

        logger.info(" Changing status from {} to {}", listing.getStatus(), status);
        listing.setStatus(status);
        Listing updatedListing = listingRepository.save(listing);

        logger.info(" Updated Listing - ID: {}, Owner: {}, New Status: {}",
            updatedListing.getListingId(), updatedListing.getOwnerId(), updatedListing.getStatus());
        logger.info("=== END UPDATE LISTING STATUS - SUCCESS ===");

        return mapToDTO(updatedListing);
    }

    /**
     * Delete a listing
     */
    @Transactional
    public void deleteListing(Long listingId, Long userId) {
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new RuntimeException("Listing not found"));

        // Verify owner
        if (!listing.getOwnerId().equals(userId)) {
            throw new RuntimeException("You can only delete your own listings");
        }

        listingRepository.delete(listing);
        logger.info("Listing deleted - ID: {}, Owner ID: {}", listingId, userId);
    }

    /**
     * Map Listing entity to ListingDTO
     */
    private ListingDTO mapToDTO(Listing listing) {
        return new ListingDTO(
            listing.getListingId(),
            listing.getOwnerId(),
            listing.getAnimalId(),
            listing.getDescription(),
            listing.getPrice(),
            listing.getStatus(),
            listing.getCreatedAt()
        );
    }
}
