package com.erumpay.notification.service;

import com.erumpay.notification.domain.entity.Notification;
import com.erumpay.notification.domain.enums.NotificationChannel;
import com.erumpay.notification.dto.NotificationReadResponse;
import com.erumpay.notification.global.exception.ErrorCode;
import com.erumpay.notification.global.exception.NotificationException;
import com.erumpay.notification.repository.NotificationRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final Clock seoulClock;

    @Transactional
    public NotificationReadResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository
                .findByNotificationIdAndUserIdAndChannel(notificationId, userId, NotificationChannel.IN_APP)
                .orElseThrow(() -> new NotificationException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markAsRead(LocalDateTime.now(seoulClock));
        return NotificationReadResponse.from(notification);
    }
}
