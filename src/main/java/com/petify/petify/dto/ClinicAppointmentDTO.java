package com.petify.petify.dto;

import java.time.LocalDateTime;

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

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
    }

    public Long getAnimalId() {
        return animalId;
    }

    public void setAnimalId(Long animalId) {
        this.animalId = animalId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetSpecies() {
        return petSpecies;
    }

    public void setPetSpecies(String petSpecies) {
        this.petSpecies = petSpecies;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
