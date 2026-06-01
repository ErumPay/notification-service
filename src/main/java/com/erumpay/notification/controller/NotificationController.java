package com.erumpay.notification.controller;

import com.erumpay.notification.dto.NotificationReadResponse;
import com.erumpay.notification.dto.NotificationPreferenceResponse;
import com.erumpay.notification.dto.NotificationPreferenceUpdateRequest;
import com.erumpay.notification.dto.NotificationResponse;
import com.erumpay.notification.dto.PageResponse;
import com.erumpay.notification.service.NotificationCommandService;
import com.erumpay.notification.service.NotificationPreferenceService;
import com.erumpay.notification.service.NotificationQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;
    private final NotificationPreferenceService notificationPreferenceService;

    @GetMapping
    public PageResponse<NotificationResponse> getNotifications(
            @RequestHeader("X-User-Id") @NotNull @Positive Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean isRead
    ) {
        return notificationQueryService.getNotifications(userId, page, size, isRead);
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationReadResponse readNotification(
            @RequestHeader("X-User-Id") @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long notificationId
    ) {
        return notificationCommandService.markAsRead(userId, notificationId);
    }

    @GetMapping("/preferences")
    public NotificationPreferenceResponse getNotificationPreference(
            @RequestHeader("X-User-Id") @NotNull @Positive Long userId
    ) {
        return NotificationPreferenceResponse.from(notificationPreferenceService.getPreference(userId));
    }

    @PatchMapping("/preferences")
    public NotificationPreferenceResponse updateNotificationPreference(
            @RequestHeader("X-User-Id") @NotNull @Positive Long userId,
            @Valid @RequestBody NotificationPreferenceUpdateRequest request
    ) {
        return NotificationPreferenceResponse.from(notificationPreferenceService.updatePreference(userId, request));
    }
}
