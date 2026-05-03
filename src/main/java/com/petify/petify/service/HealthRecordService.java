package com.petify.petify.service;

import com.petify.petify.domain.Appointment;
import com.petify.petify.domain.HealthRecord;
import com.petify.petify.dto.CreateHealthRecordRequest;
import com.petify.petify.dto.HealthRecordDTO;
import com.petify.petify.repo.AppointmentRepository;
import com.petify.petify.repo.HealthRecordContextView;
import com.petify.petify.repo.HealthRecordRepository;
import com.petify.petify.repo.PetRepository;
import com.petify.petify.repo.VetClinicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final PetRepository petRepository;
    private final VetClinicRepository vetClinicRepository;

    public HealthRecordService(HealthRecordRepository healthRecordRepository,
                               AppointmentRepository appointmentRepository,
                               PetRepository petRepository,
                               VetClinicRepository vetClinicRepository) {
        this.healthRecordRepository = healthRecordRepository;
        this.appointmentRepository = appointmentRepository;
        this.petRepository = petRepository;
        this.vetClinicRepository = vetClinicRepository;
    }

    @Transactional
    public HealthRecordDTO createHealthRecord(Long ownerId, CreateHealthRecordRequest request) {
        if (ownerId == null) {
            throw new RuntimeException("Owner is required");
        }
        if (request.getAppointmentId() == null) {
            throw new RuntimeException("Appointment is required");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new RuntimeException("Health record type is required");
        }

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
            .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getResponsibleOwner() == null
            || appointment.getResponsibleOwner().getUserId() == null
            || !appointment.getResponsibleOwner().getUserId().equals(ownerId)) {
            throw new RuntimeException("You can create health records only for your own appointments");
        }

        if (!"DONE".equals(appointment.getStatus())) {
            throw new RuntimeException("Health records can be added only after a completed appointment");
        }

        if (healthRecordRepository.existsByAppointmentAppointmentId(appointment.getAppointmentId())) {
            throw new RuntimeException("A health record already exists for this appointment");
        }

        HealthRecord record = new HealthRecord(
            appointment.getPet(),
            appointment,
            request.getType().trim(),
            request.getDescription(),
            appointment.getDateTime().toLocalDate()
        );

        return mapToDTO(healthRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<HealthRecordDTO> getHealthRecordsForPet(Long petId) {
        petRepository.findById(petId)
            .orElseThrow(() -> new RuntimeException("Pet not found"));

        return healthRecordRepository.findContextByAnimalId(petId)
            .stream()
            .map(this::mapContextToDTO)
            .toList();
    }

    private HealthRecordDTO mapContextToDTO(HealthRecordContextView record) {
        return new HealthRecordDTO(
            record.getHealthRecordId(),
            record.getAnimalId(),
            record.getAnimalName(),
            record.getAppointmentId(),
            record.getClinicId(),
            record.getClinicName(),
            record.getType(),
            record.getDescription(),
            record.getDate(),
            record.getAppointmentDateTime()
        );
    }

    private HealthRecordDTO mapToDTO(HealthRecord record) {
        String clinicName = null;
        if (record.getAppointment() != null && record.getAppointment().getClinicId() != null) {
            clinicName = vetClinicRepository.findById(record.getAppointment().getClinicId())
                .map(clinic -> clinic.getName())
                .orElse(null);
        }
        return new HealthRecordDTO(record, clinicName);
    }
}
