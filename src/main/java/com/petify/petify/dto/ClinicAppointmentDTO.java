package com.petify.petify.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ClinicAppointmentDTO {
    private Long appointmentId;
    private Long clinicId;
    private Long animalId;
    private String petName;
    private String petSpecies;
    private Long ownerId;
    private String ownerName;
    private String status;
    private LocalDateTime dateTime;
    private String label;
    private String notes;

    public ClinicAppointmentDTO() {
    }

    public ClinicAppointmentDTO(Long appointmentId, Long clinicId, Long animalId, String petName, String petSpecies,
                                Long ownerId, String ownerName, String status, LocalDateTime dateTime,
                                String label, String notes) {
        this.appointmentId = appointmentId;
        this.clinicId = clinicId;
        this.animalId = animalId;
        this.petName = petName;
        this.petSpecies = petSpecies;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.status = status;
        this.dateTime = dateTime;
        this.label = label;
        this.notes = notes;
    }

}
