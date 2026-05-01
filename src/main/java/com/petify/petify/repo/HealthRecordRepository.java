package com.petify.petify.repo;

import com.petify.petify.domain.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByPetAnimalIdOrderByDateDesc(Long animalId);

    boolean existsByAppointmentAppointmentId(Long appointmentId);
}
