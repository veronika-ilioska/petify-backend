package com.petify.petify.repo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PublicListingCardView {
    Long getListingId();
    String getStatus();
    BigDecimal getPrice();
    String getDescription();
    LocalDateTime getCreatedAt();
    Long getAnimalId();
    Long getOwnerId();
    String getAnimalName();
    String getSpecies();
    String getBreed();
    String getLocatedName();
    String getPhotoUrl();
    String getOwnerName();
    String getOwnerEmail();
    String getOwnerUsername();
}
