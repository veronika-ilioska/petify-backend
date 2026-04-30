package com.petify.petify.repo;

import com.petify.petify.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    java.util.List<Appointment> findByResponsibleOwnerUserIdOrderByDateTimeAsc(Long userId);
}
