package com.petify.petify.dto;

import java.math.BigDecimal;

public interface UserActivityRankingProjection {
    Long getUserId();
    String getUsername();
    String getEmail();
    String getName();
    String getSurname();

    Long getListingsCreated();
    Long getReviewsLeft();
    BigDecimal getAvgRatingLeft();

    Long getAppointmentsTotal();
    Long getAppointmentsDone();
    Long getAppointmentsNoShow();
    Long getAppointmentsCancelled();

    Long getFavoritesSavedAllTime();
    Long getActivityScore();
    Long getActivityRank();
}
