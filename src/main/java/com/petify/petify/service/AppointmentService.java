package com.petify.petify.service;

import com.petify.petify.domain.Appointment;
import com.petify.petify.domain.ClinicUnavailableSlot;
import com.petify.petify.domain.Notification;
import com.petify.petify.domain.Owner;
import com.petify.petify.domain.Pet;
import com.petify.petify.domain.User;
import com.petify.petify.domain.VetClinic;
import com.petify.petify.dto.AppointmentDTO;
import com.petify.petify.dto.AppointmentSlotDTO;
import com.petify.petify.dto.ClinicAppointmentDTO;
import com.petify.petify.dto.ClinicUnavailableSlotDTO;
import com.petify.petify.dto.CreateUnavailableSlotRequest;
import com.petify.petify.dto.CreateAppointmentRequest;
import com.petify.petify.dto.OwnerAppointmentDTO;
import com.petify.petify.repo.AppointmentRepository;
import com.petify.petify.repo.ClinicUnavailableSlotRepository;
import com.petify.petify.repo.NotificationRepository;
import com.petify.petify.repo.OwnerRepository;
import com.petify.petify.repo.PetRepository;
import com.petify.petify.repo.UserRepository;
import com.petify.petify.repo.VetClinicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);
    private static final LocalTime CLINIC_DAY_START = LocalTime.of(9, 0);
    private static final LocalTime CLINIC_DAY_END = LocalTime.of(17, 0);
    private static final int SLOT_MINUTES = 30;
    private static final List<String> NON_BLOCKING_STATUSES = List.of("CANCELLED", "CANCELED", "NO_SHOW");
    private static final DateTimeFormatter SLOT_LABEL_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentRepository appointmentRepository;
    private final ClinicUnavailableSlotRepository unavailableSlotRepository;
    private final NotificationRepository notificationRepository;
    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final VetClinicRepository vetClinicRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              ClinicUnavailableSlotRepository unavailableSlotRepository,
                              NotificationRepository notificationRepository,
                              OwnerRepository ownerRepository,
                              PetRepository petRepository,
                              UserRepository userRepository,
                              VetClinicRepository vetClinicRepository) {
        this.appointmentRepository = appointmentRepository;
        this.unavailableSlotRepository = unavailableSlotRepository;
        this.notificationRepository = notificationRepository;
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
        this.userRepository = userRepository;
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
        if (!isClinicSlotAvailable(request.getClinicId(), appointmentTime)) {
            throw new RuntimeException("Selected appointment slot is no longer available");
        }

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

    @Transactional
    public java.util.List<OwnerAppointmentDTO> getAppointmentsForOwner(Long userId) {
        Owner owner = ownerRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("User is not an owner. Only owners can view appointments."));

        markPastConfirmedAppointmentsDoneForOwner(owner.getUserId());

        return appointmentRepository.findByResponsibleOwnerUserIdOrderByDateTimeAsc(owner.getUserId())
            .stream()
            .map(this::mapToOwnerDTO)
            .toList();
    }

    @Transactional
    public OwnerAppointmentDTO cancelAppointmentForOwner(Long userId, Long appointmentId) {
        if (userId == null || appointmentId == null) {
            throw new RuntimeException("User and appointment are required");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getResponsibleOwner() == null
            || appointment.getResponsibleOwner().getUserId() == null
            || !appointment.getResponsibleOwner().getUserId().equals(userId)) {
            throw new RuntimeException("You can only cancel your own appointments");
        }

        if (!appointment.getDateTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Only future appointments can be cancelled");
        }

        if ("CANCELLED".equals(appointment.getStatus()) || "CANCELED".equals(appointment.getStatus())) {
            throw new RuntimeException("Appointment is already cancelled");
        }

        if ("DONE".equals(appointment.getStatus()) || "NO_SHOW".equals(appointment.getStatus())) {
            throw new RuntimeException("This appointment can no longer be cancelled");
        }

        appointment.setStatus("CANCELLED");
        Appointment saved = appointmentRepository.save(appointment);
        notifyClinicAboutCancellation(saved);

        return mapToOwnerDTO(saved);
    }

    @Transactional
    public List<ClinicAppointmentDTO> getAppointmentsForClinic(Long clinicId, LocalDate date) {
        if (clinicId == null || date == null) {
            throw new RuntimeException("Clinic and date are required");
        }

        if (!vetClinicRepository.existsById(clinicId)) {
            throw new RuntimeException("Vet clinic not found");
        }

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        markPastConfirmedAppointmentsDoneForClinic(clinicId);

        return appointmentRepository.findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(clinicId, dayStart, dayEnd.minusNanos(1))
            .stream()
            .map(this::mapToClinicDTO)
            .toList();
    }

    @Transactional
    public List<ClinicAppointmentDTO> getAppointmentsForClinicUser(Long userId, LocalDate date) {
        return getAppointmentsForClinic(resolveClinicIdForUser(userId), date);
    }

    @Transactional
    public ClinicAppointmentDTO markAppointmentNoShowForClinicUser(Long userId, Long appointmentId) {
        Long clinicId = resolveClinicIdForUser(userId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!clinicId.equals(appointment.getClinicId())) {
            throw new RuntimeException("You can only update appointments for your own clinic");
        }

        if (!"CONFIRMED".equals(appointment.getStatus()) && !"DONE".equals(appointment.getStatus())) {
            throw new RuntimeException("Only confirmed or done appointments can be marked as no-show");
        }

        if (appointment.getDateTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("An appointment can be marked as no-show only after its scheduled time");
        }

        appointment.setStatus("NO_SHOW");
        return mapToClinicDTO(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentSlotDTO> getAvailableSlots(Long clinicId, LocalDate date) {
        if (clinicId == null || date == null) {
            throw new RuntimeException("Clinic and date are required");
        }

        if (!vetClinicRepository.existsById(clinicId)) {
            throw new RuntimeException("Vet clinic not found");
        }

        LocalDateTime dayStart = date.atTime(CLINIC_DAY_START);
        LocalDateTime dayEnd = date.atTime(CLINIC_DAY_END);
        Set<LocalDateTime> bookedSlots = appointmentRepository
            .findByClinicIdAndDateTimeBetweenAndStatusNotInOrderByDateTimeAsc(
                clinicId,
                dayStart,
                dayEnd.minusNanos(1),
                NON_BLOCKING_STATUSES
            )
            .stream()
            .map(Appointment::getDateTime)
            .collect(java.util.stream.Collectors.toSet());
        Set<LocalDateTime> unavailableSlots = unavailableSlotRepository
            .findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(
                clinicId,
                dayStart,
                dayEnd.minusNanos(1)
            )
            .stream()
            .map(ClinicUnavailableSlot::getDateTime)
            .collect(java.util.stream.Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();
        return java.util.stream.Stream
            .iterate(dayStart, slot -> slot.isBefore(dayEnd), slot -> slot.plusMinutes(SLOT_MINUTES))
            .filter(slot -> !slot.isBefore(now))
            .filter(slot -> !bookedSlots.contains(slot))
            .filter(slot -> !unavailableSlots.contains(slot))
            .map(slot -> new AppointmentSlotDTO(slot, slot.format(SLOT_LABEL_FORMATTER)))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentSlotDTO> getAvailableSlotsForClinicUser(Long userId, LocalDate date) {
        return getAvailableSlots(resolveClinicIdForUser(userId), date);
    }

    @Transactional(readOnly = true)
    public List<ClinicUnavailableSlotDTO> getUnavailableSlots(Long clinicId, LocalDate date) {
        if (clinicId == null || date == null) {
            throw new RuntimeException("Clinic and date are required");
        }

        if (!vetClinicRepository.existsById(clinicId)) {
            throw new RuntimeException("Vet clinic not found");
        }

        LocalDateTime dayStart = date.atTime(CLINIC_DAY_START);
        LocalDateTime dayEnd = date.atTime(CLINIC_DAY_END);

        return unavailableSlotRepository
            .findByClinicIdAndDateTimeBetweenOrderByDateTimeAsc(clinicId, dayStart, dayEnd.minusNanos(1))
            .stream()
            .map(this::mapToUnavailableSlotDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ClinicUnavailableSlotDTO> getUnavailableSlotsForClinicUser(Long userId, LocalDate date) {
        return getUnavailableSlots(resolveClinicIdForUser(userId), date);
    }

    @Transactional
    public ClinicUnavailableSlotDTO createUnavailableSlot(Long clinicId, CreateUnavailableSlotRequest request) {
        if (clinicId == null || request.getDateTime() == null) {
            throw new RuntimeException("Clinic and date/time are required");
        }

        if (!vetClinicRepository.existsById(clinicId)) {
            throw new RuntimeException("Vet clinic not found");
        }

        LocalDateTime slotTime = LocalDateTime.parse(request.getDateTime());
        if (!isValidWorkingSlot(slotTime)) {
            throw new RuntimeException("Unavailable slot must be a future 30-minute slot between 09:00 and 17:00");
        }

        if (appointmentRepository.existsByClinicIdAndDateTimeAndStatusNotIn(clinicId, slotTime, NON_BLOCKING_STATUSES)) {
            throw new RuntimeException("Cannot block a slot that already has an appointment");
        }

        if (unavailableSlotRepository.existsByClinicIdAndDateTime(clinicId, slotTime)) {
            throw new RuntimeException("This slot is already marked unavailable");
        }

        ClinicUnavailableSlot saved = unavailableSlotRepository.save(
            new ClinicUnavailableSlot(clinicId, slotTime, request.getReason())
        );
        return mapToUnavailableSlotDTO(saved);
    }

    @Transactional
    public ClinicUnavailableSlotDTO createUnavailableSlotForClinicUser(Long userId, CreateUnavailableSlotRequest request) {
        return createUnavailableSlot(resolveClinicIdForUser(userId), request);
    }

    @Transactional
    public void deleteUnavailableSlot(Long clinicId, Long slotId) {
        ClinicUnavailableSlot slot = unavailableSlotRepository.findBySlotIdAndClinicId(slotId, clinicId)
            .orElseThrow(() -> new RuntimeException("Unavailable slot not found"));
        unavailableSlotRepository.delete(slot);
    }

    @Transactional
    public void deleteUnavailableSlotForClinicUser(Long userId, Long slotId) {
        deleteUnavailableSlot(resolveClinicIdForUser(userId), slotId);
    }

    private Long resolveClinicIdForUser(Long userId) {
        if (userId == null) {
            throw new RuntimeException("User is required");
        }

        return vetClinicRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("User is not linked to a clinic"))
            .getClinicId();
    }

    private boolean isClinicSlotAvailable(Long clinicId, LocalDateTime appointmentTime) {
        if (!isValidWorkingSlot(appointmentTime)) {
            return false;
        }

        return !appointmentRepository.existsByClinicIdAndDateTimeAndStatusNotIn(
            clinicId,
            appointmentTime,
            NON_BLOCKING_STATUSES
        ) && !unavailableSlotRepository.existsByClinicIdAndDateTime(clinicId, appointmentTime);
    }

    private boolean isValidWorkingSlot(LocalDateTime appointmentTime) {
        LocalTime time = appointmentTime.toLocalTime();
        return !appointmentTime.isBefore(LocalDateTime.now())
            && !time.isBefore(CLINIC_DAY_START)
            && time.isBefore(CLINIC_DAY_END)
            && appointmentTime.getMinute() % SLOT_MINUTES == 0
            && appointmentTime.getSecond() == 0
            && appointmentTime.getNano() == 0;
    }

    private void markPastConfirmedAppointmentsDoneForOwner(Long ownerId) {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> updatedAppointments = appointmentRepository.findByResponsibleOwnerUserIdOrderByDateTimeAsc(ownerId)
            .stream()
            .filter(appointment -> "CONFIRMED".equals(appointment.getStatus()))
            .filter(appointment -> !appointment.getDateTime().isAfter(now))
            .peek(appointment -> appointment.setStatus("DONE"))
            .toList();

        if (!updatedAppointments.isEmpty()) {
            appointmentRepository.saveAll(updatedAppointments);
        }
    }

    private void markPastConfirmedAppointmentsDoneForClinic(Long clinicId) {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> updatedAppointments = appointmentRepository
            .findByClinicIdAndStatusAndDateTimeLessThanEqual(clinicId, "CONFIRMED", now)
            .stream()
            .peek(appointment -> appointment.setStatus("DONE"))
            .toList();

        if (!updatedAppointments.isEmpty()) {
            appointmentRepository.saveAll(updatedAppointments);
        }
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

    private void notifyClinicAboutCancellation(Appointment appointment) {
        VetClinic clinic = vetClinicRepository.findById(appointment.getClinicId()).orElse(null);
        if (clinic == null || clinic.getUserId() == null) {
            logger.warn("Skipping cancellation notification: clinic {} has no linked user", appointment.getClinicId());
            return;
        }

        User clinicUser = userRepository.findById(clinic.getUserId()).orElse(null);
        if (clinicUser == null) {
            logger.warn("Skipping cancellation notification: clinic user {} not found", clinic.getUserId());
            return;
        }

        Pet pet = appointment.getPet();
        Owner owner = appointment.getResponsibleOwner();
        User ownerUser = owner != null ? owner.getUser() : null;
        String ownerName = ownerUser != null
            ? ownerUser.getFirstName() + " " + ownerUser.getLastName()
            : "An owner";
        String petName = pet != null ? pet.getName() : "a pet";
        String message = "%s cancelled the appointment for %s on %s."
            .formatted(ownerName, petName, appointment.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        notificationRepository.save(new Notification(clinicUser, "APPOINTMENT_CANCELLED", message));
    }

    private ClinicAppointmentDTO mapToClinicDTO(Appointment appointment) {
        Pet pet = appointment.getPet();
        Owner owner = appointment.getResponsibleOwner();
        var user = owner != null ? owner.getUser() : null;
        String ownerName = user != null ? user.getFirstName() + " " + user.getLastName() : null;

        return new ClinicAppointmentDTO(
            appointment.getAppointmentId(),
            appointment.getClinicId(),
            pet != null ? pet.getAnimalId() : null,
            pet != null ? pet.getName() : null,
            pet != null ? pet.getSpecies() : null,
            owner != null ? owner.getUserId() : null,
            ownerName,
            appointment.getStatus(),
            appointment.getDateTime(),
            appointment.getDateTime().format(SLOT_LABEL_FORMATTER),
            appointment.getNotes()
        );
    }

    private ClinicUnavailableSlotDTO mapToUnavailableSlotDTO(ClinicUnavailableSlot slot) {
        return new ClinicUnavailableSlotDTO(
            slot.getSlotId(),
            slot.getClinicId(),
            slot.getDateTime(),
            slot.getDateTime().format(SLOT_LABEL_FORMATTER),
            slot.getReason()
        );
    }
}
