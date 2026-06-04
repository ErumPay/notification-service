package com.erumpay.notification.service;

import com.erumpay.notification.domain.entity.Notification;
import com.erumpay.notification.domain.enums.NotificationChannel;
import com.erumpay.notification.domain.enums.NotificationType;
import com.erumpay.notification.dto.NotificationResponse;
import com.erumpay.notification.dto.PageResponse;
import com.erumpay.notification.global.exception.ErrorCode;
import com.erumpay.notification.global.exception.NotificationException;
import com.erumpay.notification.repository.NotificationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationQueryServiceTest {

    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final NotificationQueryService notificationQueryService =
            new NotificationQueryService(notificationRepository);

    @BeforeEach
    void setUp() {
        reset(notificationRepository);
    }

    @Test
    void getsAllInAppNotificationsWhenIsReadIsNull() {
        Notification notification = notification("evt_query_001", false);
        when(notificationRepository.findByUserIdAndChannel(
                eq(101L),
                eq(NotificationChannel.IN_APP),
                org.mockito.ArgumentMatchers.any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(notification), PageRequest.of(0, 20), 1));

        PageResponse<NotificationResponse> response =
                notificationQueryService.getNotifications(101L, 0, 20, null);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).channel()).isEqualTo(NotificationChannel.IN_APP);
        verify(notificationRepository).findByUserIdAndChannel(
                eq(101L),
                eq(NotificationChannel.IN_APP),
                org.mockito.ArgumentMatchers.argThat(pageable ->
                        pageable.getPageNumber() == 0
                                && pageable.getPageSize() == 20
                                && pageable.getSort().getOrderFor("createdAt") != null
                )
        );
    }

    @Test
    void filtersByReadStatus() {
        when(notificationRepository.findByUserIdAndChannelAndIsRead(
                eq(101L),
                eq(NotificationChannel.IN_APP),
                eq(false),
                org.mockito.ArgumentMatchers.any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(notification("evt_query_002", false)), PageRequest.of(0, 20), 1));

        PageResponse<NotificationResponse> response =
                notificationQueryService.getNotifications(101L, 0, 20, false);

        assertThat(response.content()).hasSize(1);
        verify(notificationRepository).findByUserIdAndChannelAndIsRead(
                eq(101L),
                eq(NotificationChannel.IN_APP),
                eq(false),
                org.mockito.ArgumentMatchers.any(Pageable.class)
        );
    }

    @Test
    void rejectsNegativePage() {
        assertThatThrownBy(() -> notificationQueryService.getNotifications(101L, -1, 20, null))
                .isInstanceOf(NotificationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void rejectsSizeOverMax() {
        assertThatThrownBy(() -> notificationQueryService.getNotifications(101L, 0, 101, null))
                .isInstanceOf(NotificationException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    private Notification notification(String eventId, boolean isRead) {
        return Notification.builder()
                .eventId(eventId)
                .userId(101L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .title("Payment completed.")
                .content("The payment was completed.")
                .paymentId(90001L)
                .channel(NotificationChannel.IN_APP)
                .isRead(isRead)
                .build();
    }
}
