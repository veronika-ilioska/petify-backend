package com.petify.petify.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Pet pet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "responsible_owner_id", nullable = false)
    private Owner responsibleOwner;

    @Column(nullable = false)
    private String status;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    private String notes;

    public Appointment() {}

    public Appointment(Long clinicId, Pet pet, Owner responsibleOwner, String status, LocalDateTime dateTime, String notes) {
        this.clinicId = clinicId;
        this.pet = pet;
        this.responsibleOwner = responsibleOwner;
        this.status = status;
        this.dateTime = dateTime;
        this.notes = notes;
    }

    public Long getAnimalId() {
        return pet != null ? pet.getAnimalId() : null;
    }

    public Long getOwnerId() {
        return responsibleOwner != null ? responsibleOwner.getUserId() : null;
    }
}

