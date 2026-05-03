package com.petify.petify.repo;
import com.petify.petify.domain.Listing;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface PublicListingRepository extends JpaRepository<Listing, Long> {

    @Query(value = """
        SELECT
            listing_id AS "listingId",
            status AS "status",
            price AS "price",
            description AS "description",
            created_at AS "createdAt",
            animal_id AS "animalId",
            listing_owner_id AS "ownerId",
            animal_name AS "animalName",
            species AS "species",
            breed AS "breed",
            located_name AS "locatedName",
            photo_url AS "photoUrl",
            owner_name AS "ownerName",
            owner_email AS "ownerEmail",
            owner_username AS "ownerUsername"
        FROM v_listings_enriched
        WHERE status = 'ACTIVE'
          AND owner_match = true
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<PublicListingCardView> findActiveListingCards();
}
