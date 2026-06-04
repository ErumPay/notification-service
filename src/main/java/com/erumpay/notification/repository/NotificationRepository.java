package com.erumpay.notification.repository;

import com.erumpay.notification.domain.entity.Notification;
import com.erumpay.notification.domain.enums.NotificationChannel;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead, Pageable pageable);

    Optional<Notification> findByNotificationIdAndUserId(Long notificationId, Long userId);

    Page<Notification> findByUserIdAndChannel(Long userId, NotificationChannel channel, Pageable pageable);

    Page<Notification> findByUserIdAndChannelAndIsRead(
            Long userId,
            NotificationChannel channel,
            Boolean isRead,
            Pageable pageable
    );

    Optional<Notification> findByNotificationIdAndUserIdAndChannel(
            Long notificationId,
            Long userId,
            NotificationChannel channel
    );

    Optional<Notification> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}
