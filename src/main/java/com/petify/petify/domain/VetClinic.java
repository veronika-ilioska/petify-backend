package com.petify.petify.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "vet_clinics")
public class VetClinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clinic_id")
    private Long clinicId;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String name;

    private String email;

    private String phone;

    private String location;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String address;

    public VetClinic() {}

}

