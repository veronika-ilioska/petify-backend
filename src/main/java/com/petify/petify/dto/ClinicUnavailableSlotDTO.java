package com.petify.petify.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
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

}
