package com.erumpay.notification.dto;

import com.erumpay.notification.domain.entity.Notification;
import com.erumpay.notification.domain.enums.NotificationChannel;
import com.erumpay.notification.domain.enums.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        NotificationType type,
        String title,
        String content,
        Long paymentId,
        Boolean isRead,
        NotificationChannel channel,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getPaymentId(),
                notification.getIsRead(),
                notification.getChannel(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
