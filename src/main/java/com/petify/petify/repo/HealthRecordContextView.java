package com.petify.petify.repo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface HealthRecordContextView {
    Long getHealthRecordId();
    Long getAnimalId();
    String getAnimalName();
    Long getAppointmentId();
    Long getClinicId();
    String getClinicName();
    String getType();
    String getDescription();
    LocalDate getDate();
    LocalDateTime getAppointmentDateTime();
}
