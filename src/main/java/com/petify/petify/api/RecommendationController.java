package com.petify.petify.api;

import com.petify.petify.dto.RecommendedListingProjection;
import com.petify.petify.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/listings/{userId}")
    public List<RecommendedListingProjection> getRecommendedListings(@PathVariable Long userId) {
        return recommendationService.getRecommendedListings(userId);
    }
}
