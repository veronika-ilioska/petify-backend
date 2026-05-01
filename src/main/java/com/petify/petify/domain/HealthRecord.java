package com.petify.petify.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "health_records")
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "healthrecord_id")
    private Long healthRecordId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Pet pet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(nullable = false)
    private String type;

    private String description;

    @Column(nullable = false)
    private LocalDate date;

    public HealthRecord() {
    }

    public HealthRecord(Pet pet, Appointment appointment, String type, String description, LocalDate date) {
        this.pet = pet;
        this.appointment = appointment;
        this.type = type;
        this.description = description;
        this.date = date;
    }
}
