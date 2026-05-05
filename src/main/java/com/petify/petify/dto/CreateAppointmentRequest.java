package com.petify.petify.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateAppointmentRequest {
    private Long clinicId;
    private Long animalId;
    private String dateTime;
    private String notes;

    public CreateAppointmentRequest() {}

    public CreateAppointmentRequest(Long clinicId, Long animalId, String dateTime, String notes) {
        this.clinicId = clinicId;
        this.animalId = animalId;
        this.dateTime = dateTime;
        this.notes = notes;
    }

}

