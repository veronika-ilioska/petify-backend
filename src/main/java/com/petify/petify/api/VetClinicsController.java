package com.petify.petify.api;

import com.petify.petify.domain.VetClinic;
import com.petify.petify.dto.VetClinicDTO;
import com.petify.petify.repo.VetClinicRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clinics")
public class VetClinicsController {

    private final VetClinicRepository vetClinicRepository;

    public VetClinicsController(VetClinicRepository vetClinicRepository) {
        this.vetClinicRepository = vetClinicRepository;
    }

    @GetMapping
    public List<VetClinicDTO> getClinics() {
        return vetClinicRepository.findAllByOrderByNameAsc()
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    private VetClinicDTO mapToDTO(VetClinic clinic) {
        return new VetClinicDTO(
            clinic.getClinicId(),
            clinic.getName(),
            clinic.getCity(),
            clinic.getAddress()
        );
    }
}

