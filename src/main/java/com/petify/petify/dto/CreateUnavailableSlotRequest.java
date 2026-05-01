package com.petify.petify.dto;

public class CreateUnavailableSlotRequest {
    private String dateTime;
    private String reason;

    public CreateUnavailableSlotRequest() {
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
