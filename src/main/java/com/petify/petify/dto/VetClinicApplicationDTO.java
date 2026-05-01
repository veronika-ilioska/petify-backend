package com.petify.petify.dto;

import com.petify.petify.domain.VetClinicApplication;

import java.time.LocalDateTime;

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

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getDenialReason() { return denialReason; }
    public void setDenialReason(String denialReason) { this.denialReason = denialReason; }
}
