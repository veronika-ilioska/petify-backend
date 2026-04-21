package com.petify.petify.dto;

import com.petify.petify.domain.UserType;
import java.time.LocalDateTime;

public class UserDTO {
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private String userType;
    private boolean isBlocked;
    private String blockedReason;
    private boolean verified;

    // Constructors
    public UserDTO() {
    }

    public UserDTO(Long userId, String username, String email, String firstName,
                   String lastName, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
        this.userType = "CLIENT"; // Default type
        this.isBlocked = false;
        this.blockedReason = "";
        this.verified = false;
    }

    public UserDTO(Long userId, String username, String email, String firstName,
                   String lastName, LocalDateTime createdAt, String userType) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
        this.userType = userType;
        this.isBlocked = false;
        this.blockedReason = "";
        this.verified = false;
    }

    public UserDTO(Long userId, String username, String email, String firstName,
                   String lastName, LocalDateTime createdAt, String userType, boolean isBlocked, String blockedReason) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
        this.userType = userType;
        this.isBlocked = isBlocked;
        this.blockedReason = blockedReason;
        this.verified = false;
    }

    public UserDTO(Long userId, String username, String email, String firstName,
                   String lastName, LocalDateTime createdAt, String userType, boolean isBlocked, String blockedReason, boolean verified) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
        this.userType = userType;
        this.isBlocked = isBlocked;
        this.blockedReason = blockedReason;
        this.verified = verified;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getBlockedReason() {
        return blockedReason;
    }

    public void setBlockedReason(String blockedReason) {
        this.blockedReason = blockedReason;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
