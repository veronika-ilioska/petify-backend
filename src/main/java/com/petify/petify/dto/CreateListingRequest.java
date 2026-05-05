package com.petify.petify.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class CreateListingRequest {
    private Long animalId;
    private String description;
    private BigDecimal price;

    public CreateListingRequest() {}

    public CreateListingRequest(Long animalId, String description, BigDecimal price) {
        this.animalId = animalId;
        this.description = description;
        this.price = price;
    }

}
