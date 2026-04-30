package com.petify.petify.dto;

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

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

