package com.petify.petify.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class CreatePetRequest {
    private String name;
    private String sex;
    private LocalDate dateOfBirth;
    private String photoUrl;
    private String type;
    private String species;
    private String breed;
    private String locatedName;

    public CreatePetRequest() {}

    public CreatePetRequest(String name, String sex, LocalDate dateOfBirth, String photoUrl,
                           String type, String species, String breed, String locatedName) {
        this.name = name;
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
        this.photoUrl = photoUrl;
        this.type = type;
        this.species = species;
        this.breed = breed;
        this.locatedName = locatedName;
    }

}
