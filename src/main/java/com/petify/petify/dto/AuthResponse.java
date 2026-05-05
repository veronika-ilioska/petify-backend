package com.petify.petify.dto;

import com.petify.petify.domain.UserType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthResponse {
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private UserType userType;
    private boolean verified;



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

}
