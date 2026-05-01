package com.petify.petify.repo;

import com.petify.petify.domain.ClinicUnavailableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicUnavailableSlotRepository extends JpaRepository<ClinicUnavailableSlot, Long> {
    List<ClinicUnavailableSlot> findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(
        Long clinicId,
        LocalDateTime start,
        LocalDateTime end
    );

    boolean existsByClinicIdAndDateTime(Long clinicId, LocalDateTime dateTime);

    Optional<ClinicUnavailableSlot> findBySlotIdAndClinicId(Long slotId, Long clinicId);
}
