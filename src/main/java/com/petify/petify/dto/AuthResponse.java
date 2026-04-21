package com.petify.petify.dto;

import com.petify.petify.domain.UserType;

public class AuthResponse {
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private UserType userType;
    private boolean verified;

    // Constructors
    public AuthResponse() {
    }

    public AuthResponse(Long userId, String username, String email, String firstName,
                       String lastName, UserType userType) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.verified = false;
    }

    public AuthResponse(Long userId, String username, String email, String firstName,
                       String lastName, UserType userType, boolean verified) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
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

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
