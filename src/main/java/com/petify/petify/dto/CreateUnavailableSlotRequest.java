package com.petify.petify.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateUnavailableSlotRequest {
    private String dateTime;
    private String reason;

    public CreateUnavailableSlotRequest() {
    }

}
