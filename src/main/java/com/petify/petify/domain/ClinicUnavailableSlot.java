package com.petify.petify.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "clinic_unavailable_slots")
public class ClinicUnavailableSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    private Long slotId;

    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ClinicUnavailableSlot() {
    }

    public ClinicUnavailableSlot(Long clinicId, LocalDateTime dateTime, String reason) {
        this.clinicId = clinicId;
        this.dateTime = dateTime;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
