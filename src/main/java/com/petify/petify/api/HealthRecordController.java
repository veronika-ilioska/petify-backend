package com.petify.petify.api;

import com.petify.petify.dto.CreateHealthRecordRequest;
import com.petify.petify.dto.HealthRecordDTO;
import com.petify.petify.service.HealthRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    public HealthRecordController(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    @GetMapping("/api/pets/{petId}/health-records")
    public ResponseEntity<?> getHealthRecordsForPet(@PathVariable Long petId) {
        try {
            List<HealthRecordDTO> records = healthRecordService.getHealthRecordsForPet(petId);
            return ResponseEntity.ok(records);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/health-records")
    public ResponseEntity<?> createHealthRecord(
        @RequestHeader("X-User-Id") Long ownerId,
        @RequestBody CreateHealthRecordRequest request
    ) {
        try {
            HealthRecordDTO record = healthRecordService.createHealthRecord(ownerId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(record);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
