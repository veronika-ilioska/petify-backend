package com.petify.petify.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VetClinicDTO {
    private Long clinicId;
    private String name;
    private String city;
    private String address;

    public VetClinicDTO() {
    }

    public VetClinicDTO(Long clinicId, String name, String city, String address) {
        this.clinicId = clinicId;
        this.name = name;
        this.city = city;
        this.address = address;
    }

}

