package com.petify.petify.repo;
import com.petify.petify.domain.Listing;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface PublicListingRepository extends JpaRepository<Listing, Long> {

    @Query(value = """
        SELECT
            v.listing_id AS "listingId",
            l.status AS "status",
            l.price AS "price",
            l.description AS "description",
            l.created_at AS "createdAt",
            v.animal_id AS "animalId",
            v.listing_owner_id AS "ownerId",
            v.animal_name AS "animalName",
            v.species AS "species",
            v.breed AS "breed",
            v.located_name AS "locatedName",
            a.photo_url AS "photoUrl",
            TRIM(CONCAT(u.name, ' ', u.surname)) AS "ownerName",
            u.email AS "ownerEmail",
            u.username AS "ownerUsername"
        FROM v_listings_enriched v
        JOIN listings l ON l.listing_id = v.listing_id
        JOIN animals a ON a.animal_id = v.animal_id
        JOIN users u ON u.user_id = v.listing_owner_id
        WHERE l.status = 'ACTIVE'
          AND v.owner_match = true
        ORDER BY l.created_at DESC
        """, nativeQuery = true)
    List<PublicListingCardView> findActiveListingCards();
}
