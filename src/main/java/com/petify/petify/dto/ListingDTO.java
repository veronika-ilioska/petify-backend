package com.petify.petify.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class ListingDTO {
    private Long listingId;
    private Long ownerId;
    private Long animalId;
    private String description;
    private BigDecimal price;
    private String status;
    private LocalDateTime createdAt;

    public ListingDTO() {}

    public ListingDTO(Long listingId, Long ownerId, Long animalId, String description, BigDecimal price, String status, LocalDateTime createdAt) {
        this.listingId = listingId;
        this.ownerId = ownerId;
        this.animalId = animalId;
        this.description = description;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }

}
