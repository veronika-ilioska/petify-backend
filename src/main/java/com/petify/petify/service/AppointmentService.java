package com.petify.petify.service;

import com.petify.petify.domain.Appointment;
import com.petify.petify.domain.Owner;
import com.petify.petify.domain.Pet;
import com.petify.petify.dto.AppointmentDTO;
import com.petify.petify.dto.CreateAppointmentRequest;
import com.petify.petify.dto.OwnerAppointmentDTO;
import com.petify.petify.repo.AppointmentRepository;
import com.petify.petify.repo.OwnerRepository;
import com.petify.petify.repo.PetRepository;
import com.petify.petify.repo.VetClinicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final VetClinicRepository vetClinicRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              OwnerRepository ownerRepository,
                              PetRepository petRepository,
                              VetClinicRepository vetClinicRepository) {
        this.appointmentRepository = appointmentRepository;
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
        this.vetClinicRepository = vetClinicRepository;
    }

    @Transactional
    public AppointmentDTO createAppointment(Long userId, CreateAppointmentRequest request) {
        if (request.getClinicId() == null || request.getAnimalId() == null || request.getDateTime() == null) {
            throw new RuntimeException("Clinic, pet, and date/time are required");
        }

        Owner owner = ownerRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("User is not an owner. Only owners can create appointments."));

        Pet pet = petRepository.findById(request.getAnimalId())
            .orElseThrow(() -> new RuntimeException("Pet not found"));

        if (pet.getOwner() == null || pet.getOwner().getUserId() == null || !pet.getOwner().getUserId().equals(userId)) {
            throw new RuntimeException("You can only create appointments for your own pets");
        }

        if (!vetClinicRepository.existsById(request.getClinicId())) {
            throw new RuntimeException("Vet clinic not found");
        }

        LocalDateTime appointmentTime = LocalDateTime.parse(request.getDateTime());

        Appointment appointment = new Appointment(
            request.getClinicId(),
            pet,
            owner,
            "CONFIRMED",
            appointmentTime,
            request.getNotes()
        );

        Appointment saved = appointmentRepository.save(appointment);

        logger.info("Appointment created - ID: {}, Owner: {}, Pet: {}, Clinic: {}",
            saved.getAppointmentId(), userId, saved.getAnimalId(), saved.getClinicId());

        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public java.util.List<OwnerAppointmentDTO> getAppointmentsForOwner(Long userId) {
        Owner owner = ownerRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("User is not an owner. Only owners can view appointments."));

        return appointmentRepository.findByResponsibleOwnerUserIdOrderByDateTimeAsc(owner.getUserId())
            .stream()
            .map(this::mapToOwnerDTO)
            .toList();
    }

    private AppointmentDTO mapToDTO(Appointment appointment) {
        return new AppointmentDTO(
            appointment.getAppointmentId(),
            appointment.getClinicId(),
            appointment.getAnimalId(),
            appointment.getOwnerId(),
            appointment.getStatus(),
            appointment.getDateTime(),
            appointment.getNotes()
        );
    }

    private OwnerAppointmentDTO mapToOwnerDTO(Appointment appointment) {
        Pet pet = appointment.getPet();
        var clinic = vetClinicRepository.findById(appointment.getClinicId()).orElse(null);

        return new OwnerAppointmentDTO(
            appointment.getAppointmentId(),
            appointment.getClinicId(),
            clinic != null ? clinic.getName() : null,
            clinic != null ? clinic.getCity() : null,
            clinic != null ? clinic.getAddress() : null,
            pet != null ? pet.getAnimalId() : null,
            pet != null ? pet.getName() : null,
            pet != null ? pet.getSpecies() : null,
            pet != null ? pet.getPhotoUrl() : null,
            appointment.getStatus(),
            appointment.getDateTime(),
            appointment.getNotes()
        );
    }
}
