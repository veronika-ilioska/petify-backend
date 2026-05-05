package com.petify.petify.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class AdminListingsPageDTO {
    private List<ListingDTO> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private long activeListings;
    private long soldListings;

    public AdminListingsPageDTO(List<ListingDTO> items,
                                int page,
                                int size,
                                long totalItems,
                                int totalPages,
                                boolean hasNext,
                                boolean hasPrevious,
                                long activeListings,
                                long soldListings) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
        this.activeListings = activeListings;
        this.soldListings = soldListings;
    }

}
