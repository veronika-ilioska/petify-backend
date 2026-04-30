package com.petify.petify.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class AppointmentDTO {
    //mozebi ne mi ni treba , se e isto ova so Appointment entity, samo bez relations, i so Long ids namesto objekti
    private Long appointmentId;
    private Long clinicId;
    private Long animalId;
    private Long ownerId;
    private String status;
    private LocalDateTime dateTime;
    private String notes;

    public AppointmentDTO() {}

    public AppointmentDTO(Long appointmentId, Long clinicId, Long animalId, Long ownerId, String status, LocalDateTime dateTime, String notes) {
        this.appointmentId = appointmentId;
        this.clinicId = clinicId;
        this.animalId = animalId;
        this.ownerId = ownerId;
        this.status = status;
        this.dateTime = dateTime;
        this.notes = notes;
    }

}

