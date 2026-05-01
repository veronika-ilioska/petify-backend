package com.petify.petify.dto;

import java.time.LocalDateTime;

public class ClinicUnavailableSlotDTO {
    private Long slotId;
    private Long clinicId;
    private LocalDateTime dateTime;
    private String label;
    private String reason;

    public ClinicUnavailableSlotDTO() {
    }

    public ClinicUnavailableSlotDTO(Long slotId, Long clinicId, LocalDateTime dateTime, String label, String reason) {
        this.slotId = slotId;
        this.clinicId = clinicId;
        this.dateTime = dateTime;
        this.label = label;
        this.reason = reason;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
