package com.petify.petify.service;

import com.petify.petify.dto.UserActivityRankingProjection;
import com.petify.petify.repo.AnalyticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @Transactional(readOnly = true)
    public List<UserActivityRankingProjection> getTopActiveUsers(
            LocalDateTime startTs,
            LocalDateTime endTs
    ) {
        return analyticsRepository.getTopActiveUsers(startTs, endTs);
    }
}
