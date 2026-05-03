package com.petify.petify.repo;

import com.petify.petify.domain.Listing;
import com.petify.petify.domain.Owner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByOwner(Owner owner);
    List<Listing> findByStatus(String status);
    Page<Listing> findByStatus(String status, Pageable pageable);
    Page<Listing> findByStatusIgnoreCase(String status, Pageable pageable);
    long countByStatus(String status);

    @Query("""
        SELECT l
        FROM Listing l
        WHERE (:status IS NULL OR UPPER(TRIM(l.status)) = UPPER(TRIM(:status)))
          AND (:minPrice IS NULL OR l.price >= :minPrice)
          AND (:maxPrice IS NULL OR l.price <= :maxPrice)
        """)
    Page<Listing> findAdminListings(
            @Param("status") String status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query(value = """
        WITH
            my_likes AS (
                SELECT fl.listing_id
                FROM favorite_listings fl
                WHERE fl.client_id = :user_id
            ),

            my_recent_likes AS (
                SELECT fl.listing_id
                FROM favorite_listings fl
                         JOIN listings l ON l.listing_id = fl.listing_id
                WHERE fl.client_id = :user_id
                ORDER BY l.created_at DESC
                LIMIT 10
            ),

            similar_users AS (
                SELECT
                    fl2.client_id AS other_user_id,
                    COUNT(*)      AS overlap_likes
                FROM favorite_listings fl2
                         JOIN my_likes ml ON ml.listing_id = fl2.listing_id
                WHERE fl2.client_id <> :user_id
                GROUP BY fl2.client_id
                HAVING COUNT(*) > 0
            ),

            cf_candidates AS (
                SELECT
                    fl.listing_id,
                    SUM(su.overlap_likes) AS cf_score,
                    COUNT(DISTINCT su.other_user_id) AS liked_by_similar_users
                FROM similar_users su
                         JOIN favorite_listings fl
                              ON fl.client_id = su.other_user_id
                         LEFT JOIN my_likes ml
                                   ON ml.listing_id = fl.listing_id
                WHERE ml.listing_id IS NULL
                GROUP BY fl.listing_id
            ),

            content_candidates AS (
                SELECT
                    l2.listing_id,
                    COUNT(*) AS content_score
                FROM my_recent_likes r
                         JOIN listings l1 ON l1.listing_id = r.listing_id
                         JOIN animals a1  ON a1.animal_id = l1.animal_id

                         JOIN listings l2 ON l2.listing_id <> l1.listing_id
                         JOIN animals a2  ON a2.animal_id = l2.animal_id

                         LEFT JOIN my_likes ml ON ml.listing_id = l2.listing_id
                WHERE ml.listing_id IS NULL
                  AND (
                    a2.species = a1.species
                        OR a2.breed = a1.breed
                        OR a2.located_name = a1.located_name
                    )
                GROUP BY l2.listing_id
            ),

            merged AS (
                SELECT
                    COALESCE(cf.listing_id, cc.listing_id) AS listing_id,
                    COALESCE(cf.cf_score, 0)              AS cf_score,
                    COALESCE(cf.liked_by_similar_users, 0) AS liked_by_similar_users,
                    COALESCE(cc.content_score, 0)         AS content_score
                FROM cf_candidates cf
                         FULL OUTER JOIN content_candidates cc
                                         ON cc.listing_id = cf.listing_id
            )

        SELECT
            l.listing_id,
            a.name AS title,
            a.species,
            a.breed,
            a.located_name AS location,
            l.created_at,

            m.cf_score,
            m.liked_by_similar_users,
            m.content_score,

            (m.cf_score * 3 + m.content_score * 2) AS final_score

        FROM merged m
                 JOIN listings l ON l.listing_id = m.listing_id
                 JOIN animals  a ON a.animal_id = l.animal_id
        WHERE l.status = 'ACTIVE'
          AND l.owner_id <> :user_id
        ORDER BY final_score DESC, l.created_at DESC
        LIMIT 20
        """, nativeQuery = true)
    List<Object[]> findRecommendedListings(@Param("user_id") Long userId);
}
