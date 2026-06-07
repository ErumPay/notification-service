package com.erumpay.notification.dto;

import com.erumpay.notification.domain.enums.NotificationType;
import java.time.LocalDateTime;

public record NotificationEventMessage(
        String eventId,
        NotificationType eventType,
        Long userId,
        String title,
        String content,
        Long paymentId,
        LocalDateTime occurredAt
) {
}
