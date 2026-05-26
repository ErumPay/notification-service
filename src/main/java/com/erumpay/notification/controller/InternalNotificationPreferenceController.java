package com.erumpay.notification.controller;

import com.erumpay.notification.dto.NotificationPreferenceResponse;
import com.erumpay.notification.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/users")
public class InternalNotificationPreferenceController {

    private final NotificationPreferenceService notificationPreferenceService;

    @PostMapping("/{userId}/notification-preferences")
    public ResponseEntity<NotificationPreferenceResponse> createDefaultPreference(@PathVariable Long userId) {
        boolean alreadyExists = notificationPreferenceService.existsByUserId(userId);
        NotificationPreferenceResponse response = NotificationPreferenceResponse.from(
                notificationPreferenceService.getOrCreateDefault(userId)
        );
        HttpStatus status = alreadyExists ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }
}
