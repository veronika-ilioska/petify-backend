package com.petify.petify.repo;

import com.petify.petify.domain.User;
import com.petify.petify.dto.RecommendedListingProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM get_recommended_listings_for_user(:userId)", nativeQuery = true)
    List<RecommendedListingProjection> getRecommendedListings(@Param("userId") Long userId);
}
