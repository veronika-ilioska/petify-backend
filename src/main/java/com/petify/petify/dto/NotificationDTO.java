package com.petify.petify.dto;

import com.petify.petify.domain.Notification;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Long notificationId;
    private String type;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public NotificationDTO() {
    }

    public NotificationDTO(Notification notification) {
        this.notificationId = notification.getNotificationId();
        this.type = notification.getType();
        this.message = notification.getMessage();
        this.isRead = notification.getIsRead();
        this.createdAt = notification.getCreatedAt();
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
