package com.petify.petify.api;

import com.petify.petify.dto.AppointmentDTO;
import com.petify.petify.dto.AppointmentSlotDTO;
import com.petify.petify.dto.ClinicAppointmentDTO;
import com.petify.petify.dto.ClinicUnavailableSlotDTO;
import com.petify.petify.dto.CreateUnavailableSlotRequest;
import com.petify.petify.dto.CreateAppointmentRequest;
import com.petify.petify.service.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentsController {

    private final AppointmentService appointmentService;

    public AppointmentsController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(
        @RequestHeader("X-User-Id") Long userId,
        @RequestBody CreateAppointmentRequest request) {
        try {
            AppointmentDTO appointment = appointmentService.createAppointment(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyAppointments(@RequestHeader("X-User-Id") Long userId) {
        try {
            List<?> appointments = appointmentService.getAppointmentsForOwner(userId);
            return ResponseEntity.ok(appointments);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/clinics/{clinicId}/available-slots")
    public ResponseEntity<?> getAvailableSlots(
        @PathVariable Long clinicId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<AppointmentSlotDTO> slots = appointmentService.getAvailableSlots(clinicId, date);
            return ResponseEntity.ok(slots);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-clinic")
    public ResponseEntity<?> getMyClinicAppointments(
        @RequestHeader("X-User-Id") Long userId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<ClinicAppointmentDTO> appointments = appointmentService.getAppointmentsForClinicUser(userId, date);
            return ResponseEntity.ok(appointments);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-clinic/available-slots")
    public ResponseEntity<?> getMyClinicAvailableSlots(
        @RequestHeader("X-User-Id") Long userId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<AppointmentSlotDTO> slots = appointmentService.getAvailableSlotsForClinicUser(userId, date);
            return ResponseEntity.ok(slots);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-clinic/unavailable-slots")
    public ResponseEntity<?> getMyClinicUnavailableSlots(
        @RequestHeader("X-User-Id") Long userId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<ClinicUnavailableSlotDTO> slots = appointmentService.getUnavailableSlotsForClinicUser(userId, date);
            return ResponseEntity.ok(slots);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/my-clinic/unavailable-slots")
    public ResponseEntity<?> createMyClinicUnavailableSlot(
        @RequestHeader("X-User-Id") Long userId,
        @RequestBody CreateUnavailableSlotRequest request) {
        try {
            ClinicUnavailableSlotDTO slot = appointmentService.createUnavailableSlotForClinicUser(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(slot);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/my-clinic/unavailable-slots/{slotId}")
    public ResponseEntity<?> deleteMyClinicUnavailableSlot(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable Long slotId) {
        try {
            appointmentService.deleteUnavailableSlotForClinicUser(userId, slotId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
