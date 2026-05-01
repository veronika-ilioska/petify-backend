package com.petify.petify.dto;

import java.time.LocalDateTime;

public class AppointmentSlotDTO {
    private LocalDateTime dateTime;
    private String label;

    public AppointmentSlotDTO() {
    }

    public AppointmentSlotDTO(LocalDateTime dateTime, String label) {
        this.dateTime = dateTime;
        this.label = label;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
