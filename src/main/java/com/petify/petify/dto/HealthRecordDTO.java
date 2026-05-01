package com.petify.petify.dto;

import com.petify.petify.domain.HealthRecord;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class HealthRecordDTO {
    private Long healthRecordId;
    private Long animalId;
    private String animalName;
    private Long appointmentId;
    private Long clinicId;
    private String clinicName;
    private String type;
    private String description;
    private LocalDate date;
    private LocalDateTime appointmentDateTime;

    public HealthRecordDTO(HealthRecord record, String clinicName) {
        this.healthRecordId = record.getHealthRecordId();
        this.animalId = record.getPet() != null ? record.getPet().getAnimalId() : null;
        this.animalName = record.getPet() != null ? record.getPet().getName() : null;
        this.appointmentId = record.getAppointment() != null ? record.getAppointment().getAppointmentId() : null;
        this.clinicId = record.getAppointment() != null ? record.getAppointment().getClinicId() : null;
        this.clinicName = clinicName;
        this.type = record.getType();
        this.description = record.getDescription();
        this.date = record.getDate();
        this.appointmentDateTime = record.getAppointment() != null ? record.getAppointment().getDateTime() : null;
    }
}
