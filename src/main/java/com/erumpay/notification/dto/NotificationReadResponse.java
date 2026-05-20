package com.erumpay.notification.dto;

import com.erumpay.notification.domain.entity.Notification;
import java.time.LocalDateTime;

public record NotificationReadResponse(
        Long notificationId,
        Boolean isRead,
        LocalDateTime readAt
) {

    public static NotificationReadResponse from(Notification notification) {
        return new NotificationReadResponse(
                notification.getNotificationId(),
                notification.getIsRead(),
                notification.getReadAt()
        );
    }
}
