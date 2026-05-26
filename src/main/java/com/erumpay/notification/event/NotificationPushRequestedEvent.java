package com.erumpay.notification.event;

import com.erumpay.notification.domain.enums.NotificationType;

public record NotificationPushRequestedEvent(
        Long notificationId,
        Long userId,
        NotificationType type,
        String title,
        String content,
        Long paymentId,
        Boolean pushEnabled,
        Boolean nightBlocked
) {
}
