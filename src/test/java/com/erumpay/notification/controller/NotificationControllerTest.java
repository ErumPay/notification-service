package com.erumpay.notification.controller;

import com.erumpay.notification.domain.entity.NotificationPreference;
import com.erumpay.notification.domain.enums.NotificationChannel;
import com.erumpay.notification.domain.enums.NotificationType;
import com.erumpay.notification.dto.NotificationReadResponse;
import com.erumpay.notification.dto.NotificationPreferenceResponse;
import com.erumpay.notification.dto.NotificationPreferenceUpdateRequest;
import com.erumpay.notification.dto.NotificationResponse;
import com.erumpay.notification.dto.PageResponse;
import com.erumpay.notification.service.NotificationCommandService;
import com.erumpay.notification.service.NotificationPreferenceService;
import com.erumpay.notification.service.NotificationQueryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationControllerTest {

    private final NotificationQueryService notificationQueryService = mock(NotificationQueryService.class);
    private final NotificationCommandService notificationCommandService = mock(NotificationCommandService.class);
    private final NotificationPreferenceService notificationPreferenceService = mock(NotificationPreferenceService.class);
    private final NotificationController notificationController =
            new NotificationController(
                    notificationQueryService,
                    notificationCommandService,
                    notificationPreferenceService
            );

    @Test
    void getsNotifications() {
        PageResponse<NotificationResponse> expected = new PageResponse<>(
                List.of(new NotificationResponse(
                        1L,
                        NotificationType.PAYMENT_COMPLETED,
                        "Payment completed.",
                        "The payment was completed.",
                        90001L,
                        false,
                        NotificationChannel.IN_APP,
                        LocalDateTime.of(2026, 5, 27, 10, 0),
                        null
                )),
                0,
                20,
                1L,
                1,
                true,
                true
        );
        when(notificationQueryService.getNotifications(101L, 0, 20, false)).thenReturn(expected);

        PageResponse<NotificationResponse> response =
                notificationController.getNotifications(101L, 0, 20, false);

        assertThat(response).isSameAs(expected);
        verify(notificationQueryService).getNotifications(101L, 0, 20, false);
    }

    @Test
    void readsNotification() {
        NotificationReadResponse expected = new NotificationReadResponse(
                1L,
                true,
                LocalDateTime.of(2026, 5, 27, 10, 0)
        );
        when(notificationCommandService.markAsRead(101L, 1L)).thenReturn(expected);

        NotificationReadResponse response = notificationController.readNotification(101L, 1L);

        assertThat(response).isSameAs(expected);
        verify(notificationCommandService).markAsRead(101L, 1L);
    }

    @Test
    void getsNotificationPreference() {
        NotificationPreference preference = NotificationPreference.defaultFor(101L);
        when(notificationPreferenceService.getPreference(101L)).thenReturn(preference);

        NotificationPreferenceResponse response = notificationController.getNotificationPreference(101L);

        assertThat(response.userId()).isEqualTo(101L);
        assertThat(response.pushEnabled()).isTrue();
        assertThat(response.nightBlocked()).isFalse();
        verify(notificationPreferenceService).getPreference(101L);
    }

    @Test
    void updatesNotificationPreference() {
        NotificationPreferenceUpdateRequest request = new NotificationPreferenceUpdateRequest(
                true,
                false,
                true,
                true,
                false,
                true,
                true
        );
        NotificationPreference preference = NotificationPreference.defaultFor(101L);
        preference.update(
                request.pushEnabled(),
                request.cardEnabled(),
                request.paymentEnabled(),
                request.dutchpayEnabled(),
                request.remoteEnabled(),
                request.friendEnabled(),
                request.nightBlocked()
        );
        when(notificationPreferenceService.updatePreference(101L, request)).thenReturn(preference);

        NotificationPreferenceResponse response =
                notificationController.updateNotificationPreference(101L, request);

        assertThat(response.cardEnabled()).isFalse();
        assertThat(response.remoteEnabled()).isFalse();
        assertThat(response.nightBlocked()).isTrue();
        verify(notificationPreferenceService).updatePreference(101L, request);
    }
}
