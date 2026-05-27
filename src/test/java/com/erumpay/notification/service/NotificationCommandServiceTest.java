package com.erumpay.notification.service;

import com.erumpay.notification.domain.entity.Notification;
import com.erumpay.notification.domain.enums.NotificationChannel;
import com.erumpay.notification.domain.enums.NotificationType;
import com.erumpay.notification.dto.NotificationReadResponse;
import com.erumpay.notification.global.exception.ErrorCode;
import com.erumpay.notification.global.exception.NotificationException;
import com.erumpay.notification.repository.NotificationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class NotificationCommandServiceTest {

    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-05-27T01:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final NotificationCommandService notificationCommandService =
            new NotificationCommandService(notificationRepository, clock);

    @BeforeEach
    void setUp() {
        reset(notificationRepository);
    }

    @Test
    void marksNotificationAsRead() {
        Notification notification = unreadNotification();
        when(notificationRepository.findByNotificationIdAndUserIdAndChannel(1L, 101L, NotificationChannel.IN_APP))
                .thenReturn(Optional.of(notification));

        NotificationReadResponse response = notificationCommandService.markAsRead(101L, 1L);

        assertThat(response.notificationId()).isNull();
        assertThat(response.isRead()).isTrue();
        assertThat(response.readAt()).isEqualTo(LocalDateTime.of(2026, 5, 27, 10, 0));
    }

    @Test
    void keepsReadAtWhenAlreadyRead() {
        LocalDateTime originalReadAt = LocalDateTime.of(2026, 5, 26, 10, 0);
        Notification notification = Notification.builder()
                .eventId("evt_read_001")
                .userId(101L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .title("Payment completed.")
                .content("The payment was completed.")
                .paymentId(90001L)
                .channel(NotificationChannel.IN_APP)
                .isRead(true)
                .readAt(originalReadAt)
                .build();
        when(notificationRepository.findByNotificationIdAndUserIdAndChannel(1L, 101L, NotificationChannel.IN_APP))
                .thenReturn(Optional.of(notification));

        NotificationReadResponse response = notificationCommandService.markAsRead(101L, 1L);

        assertThat(response.isRead()).isTrue();
        assertThat(response.readAt()).isEqualTo(originalReadAt);
    }

    @Test
    void throwsWhenNotificationDoesNotBelongToUser() {
        when(notificationRepository.findByNotificationIdAndUserIdAndChannel(1L, 101L, NotificationChannel.IN_APP))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationCommandService.markAsRead(101L, 1L))
                .isInstanceOf(NotificationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    private Notification unreadNotification() {
        return Notification.builder()
                .eventId("evt_unread_001")
                .userId(101L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .title("Payment completed.")
                .content("The payment was completed.")
                .paymentId(90001L)
                .channel(NotificationChannel.IN_APP)
                .isRead(false)
                .build();
    }
}
