package com.petify.petify.repo;

import com.petify.petify.domain.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByPetAnimalIdOrderByDateDesc(Long animalId);

    @Query(value = """
        SELECT
            h.healthrecord_id AS "healthRecordId",
            h.animal_id AS "animalId",
            h.animal_name AS "animalName",
            h.appointment_id AS "appointmentId",
            h.clinic_id AS "clinicId",
            vc.name AS "clinicName",
            h.type AS "type",
            h.description AS "description",
            h.date AS "date",
            h.date_time AS "appointmentDateTime"
        FROM v_health_records_with_context h
        LEFT JOIN vet_clinics vc ON vc.clinic_id = h.clinic_id
        WHERE h.animal_id = :animalId
        ORDER BY h.date DESC, h.healthrecord_id DESC
        """, nativeQuery = true)
    List<HealthRecordContextView> findContextByAnimalId(@Param("animalId") Long animalId);

    boolean existsByAppointmentAppointmentId(Long appointmentId);
}
