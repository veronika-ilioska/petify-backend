package com.petify.petify.dto;

import com.petify.petify.domain.Pet;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class AnimalResponseDTO {

    private Long animalId;
    private String name;
    private String sex;
    private LocalDate dateOfBirth;
    private String photoUrl;
    private String type;
    private String species;
    private String breed;
    private String locatedName;
    private Long ownerUserId;

    public AnimalResponseDTO(Pet animal) {
        this.animalId = animal.getAnimalId();
        this.name = animal.getName();
        this.sex = animal.getSex();
        this.dateOfBirth = animal.getDateOfBirth();
        this.photoUrl = animal.getPhotoUrl();
        this.type = animal.getType();
        this.species = animal.getSpecies();
        this.breed = animal.getBreed();
        this.locatedName = animal.getLocatedName();
        this.ownerUserId = animal.getOwner().getUser().getUserId();
    }

}

