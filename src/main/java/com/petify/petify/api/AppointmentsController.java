package com.petify.petify.api;

import com.petify.petify.dto.AppointmentDTO;
import com.petify.petify.dto.CreateAppointmentRequest;
import com.petify.petify.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
