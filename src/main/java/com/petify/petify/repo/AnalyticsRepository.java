package com.petify.petify.repo;

import com.petify.petify.domain.User;
import com.petify.petify.dto.UserActivityRankingProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsRepository extends JpaRepository<User, Long> {

    @Query(value = """
        SELECT *
        FROM get_top_active_users(:startTs, :endTs)
        """, nativeQuery = true)
    List<UserActivityRankingProjection> getTopActiveUsers(
            @Param("startTs") LocalDateTime startTs,
            @Param("endTs") LocalDateTime endTs
    );
}
