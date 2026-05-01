package com.petify.petify.api;

import com.petify.petify.dto.NotificationDTO;
import com.petify.petify.repo.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {

    private final NotificationRepository notificationRepository;

    public NotificationsController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(@RequestHeader("X-User-Id") Long userId) {
        List<NotificationDTO> notifications = notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(NotificationDTO::new)
            .toList();
        return ResponseEntity.ok(notifications);
    }
}
