package com.petify.petify.repo;

import com.petify.petify.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    java.util.List<Appointment> findByResponsibleOwnerUserIdOrderByDateTimeAsc(Long userId);

    java.util.List<Appointment> findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(
        Long clinicId,
        LocalDateTime start,
        LocalDateTime end
    );

    java.util.List<Appointment> findByClinicIdAndDateTimeBetweenAndStatusNotInOrderByDateTimeAsc(
        Long clinicId,
        LocalDateTime start,
        LocalDateTime end,
        Collection<String> excludedStatuses
    );

    java.util.List<Appointment> findByClinicIdAndStatusAndDateTimeLessThanEqual(
        Long clinicId,
        String status,
        LocalDateTime dateTime
    );

    boolean existsByClinicIdAndDateTimeAndStatusNotIn(
        Long clinicId,
        LocalDateTime dateTime,
        Collection<String> excludedStatuses
    );

    boolean existsByResponsibleOwnerUserIdAndClinicIdAndStatus(
        Long userId,
        Long clinicId,
        String status
    );
}
