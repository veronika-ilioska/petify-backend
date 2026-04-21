package com.petify.petify.dto;

import java.time.LocalDateTime;

public interface RecommendedListingProjection {
    Long getListingId();
    String getTitle();
    String getSpecies();
    String getBreed();
    String getLocation();
    LocalDateTime getCreatedAt();
    Long getCfScore();
    Long getLikedBySimilarUsers();
    Long getContentScore();
    Long getFinalScore();
}
