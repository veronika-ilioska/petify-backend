package com.petify.petify.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class AppointmentSlotDTO {
    private LocalDateTime dateTime;
    private String label;

    public AppointmentSlotDTO() {
    }

    public AppointmentSlotDTO(LocalDateTime dateTime, String label) {
        this.dateTime = dateTime;
        this.label = label;
    }

}
