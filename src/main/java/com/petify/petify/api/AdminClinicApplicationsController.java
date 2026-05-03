package com.petify.petify.api;

import com.petify.petify.domain.VetClinic;
import com.petify.petify.domain.VetClinicApplication;
import com.petify.petify.dto.VetClinicApplicationDTO;
import com.petify.petify.repo.AdminRepository;
import com.petify.petify.repo.VetClinicApplicationRepository;
import com.petify.petify.repo.VetClinicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/clinic-applications")
public class AdminClinicApplicationsController {

    private final VetClinicApplicationRepository applicationRepository;
    private final VetClinicRepository clinicRepository;
    private final AdminRepository adminRepository;

    public AdminClinicApplicationsController(VetClinicApplicationRepository applicationRepository,
                                             VetClinicRepository clinicRepository,
                                             AdminRepository adminRepository) {
        this.applicationRepository = applicationRepository;
        this.clinicRepository = clinicRepository;
        this.adminRepository = adminRepository;
    }

    private boolean isAdmin(Long userId) {
        return userId != null && adminRepository.existsById(userId);
    }

    @GetMapping
    public ResponseEntity<List<VetClinicApplicationDTO>> getApplications(@RequestHeader("X-User-Id") Long adminUserId) {
        if (!isAdmin(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<VetClinicApplicationDTO> applications = applicationRepository.findAllByOrderBySubmittedAtDesc()
            .stream()
            .map(VetClinicApplicationDTO::new)
            .toList();
        return ResponseEntity.ok(applications);
    }

    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<?> approveApplication(
        @RequestHeader("X-User-Id") Long adminUserId,
        @PathVariable Long applicationId) {
        try {
            if (!isAdmin(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Admin access required"));
            }
            VetClinicApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

            application.setStatus("APPROVED");
            application.setReviewedAt(LocalDateTime.now());
            application.setReviewedBy(adminUserId);
            application.setDenialReason(null);
            VetClinicApplication saved = applicationRepository.save(application);

            boolean clinicExists = clinicRepository.findAll().stream()
                .anyMatch(clinic -> applicationId.equals(clinic.getApplicationId()));
            if (!clinicExists) {
                VetClinic clinic = new VetClinic();
                clinic.setApplicationId(application.getApplicationId());
                clinic.setName(application.getName());
                clinic.setEmail(application.getEmail());
                clinic.setPhone(application.getPhone());
                clinic.setCity(application.getCity());
                clinic.setAddress(application.getAddress());
                clinicRepository.save(clinic);
            }

            return ResponseEntity.ok(new VetClinicApplicationDTO(saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{applicationId}/deny")
    public ResponseEntity<?> denyApplication(
        @RequestHeader("X-User-Id") Long adminUserId,
        @PathVariable Long applicationId,
        @RequestBody Map<String, String> request) {
        try {
            if (!isAdmin(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Admin access required"));
            }
            VetClinicApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

            application.setStatus("DENIED");
            application.setReviewedAt(LocalDateTime.now());
            application.setReviewedBy(adminUserId);
            application.setDenialReason(request.getOrDefault("denialReason", ""));
            return ResponseEntity.ok(new VetClinicApplicationDTO(applicationRepository.save(application)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
