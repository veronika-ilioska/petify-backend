package com.petify.petify.dto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
public class OwnerAppointmentDTO {
    private Long appointmentId;
    private Long clinicId;
    private String clinicName;
    private String clinicCity;
    private String clinicAddress;
    private Long animalId;
    private String petName;
    private String petSpecies;
    private String petPhotoUrl;
    private String status;
    private LocalDateTime dateTime;
    private String notes;

    public OwnerAppointmentDTO() {
    }

    public OwnerAppointmentDTO(Long appointmentId, Long clinicId, String clinicName, String clinicCity, String clinicAddress,
                               Long animalId, String petName, String petSpecies, String petPhotoUrl,
                               String status, LocalDateTime dateTime, String notes) {
        this.appointmentId = appointmentId;
        this.clinicId = clinicId;
        this.clinicName = clinicName;
        this.clinicCity = clinicCity;
        this.clinicAddress = clinicAddress;
        this.animalId = animalId;
        this.petName = petName;
        this.petSpecies = petSpecies;
        this.petPhotoUrl = petPhotoUrl;
        this.status = status;
        this.dateTime = dateTime;
        this.notes = notes;
    }

}

