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
            healthrecord_id AS "healthRecordId",
            animal_id AS "animalId",
            animal_name AS "animalName",
            appointment_id AS "appointmentId",
            clinic_id AS "clinicId",
            clinic_name AS "clinicName",
            type AS "type",
            description AS "description",
            date AS "date",
            date_time AS "appointmentDateTime"
        FROM v_health_records_with_context
        WHERE animal_id = :animalId
        ORDER BY date DESC, healthrecord_id DESC
        """, nativeQuery = true)
    List<HealthRecordContextView> findContextByAnimalId(@Param("animalId") Long animalId);

    boolean existsByAppointmentAppointmentId(Long appointmentId);
}
