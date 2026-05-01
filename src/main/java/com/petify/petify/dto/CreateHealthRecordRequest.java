package com.petify.petify.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateHealthRecordRequest {
    private Long appointmentId;
    private String type;
    private String description;
}
