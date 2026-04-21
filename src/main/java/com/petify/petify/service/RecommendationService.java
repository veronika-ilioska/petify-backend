package com.petify.petify.service;

import com.petify.petify.dto.RecommendedListingProjection;
import com.petify.petify.repo.RecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    public RecommendationService(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional(readOnly = true)
    public List<RecommendedListingProjection> getRecommendedListings(Long userId) {
        return recommendationRepository.getRecommendedListings(userId);
    }
}


