package com.petify.petify.dto;

import com.petify.petify.domain.VetClinicApplication;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class VetClinicApplicationDTO {
    private Long applicationId;
    private String name;
    private String email;
    private String phone;
    private String city;
    private String address;
    private LocalDateTime submittedAt;
    private String status;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private String denialReason;

    public VetClinicApplicationDTO() {
    }

    public VetClinicApplicationDTO(VetClinicApplication application) {
        this.applicationId = application.getApplicationId();
        this.name = application.getName();
        this.email = application.getEmail();
        this.phone = application.getPhone();
        this.city = application.getCity();
        this.address = application.getAddress();
        this.submittedAt = application.getSubmittedAt();
        this.status = application.getStatus();
        this.reviewedAt = application.getReviewedAt();
        this.reviewedBy = application.getReviewedBy();
        this.denialReason = application.getDenialReason();
    }

}
