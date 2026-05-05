package com.petify.petify.dto;

import com.petify.petify.domain.UserType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
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


}
