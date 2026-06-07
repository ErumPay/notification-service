package com.erumpay.notification.service;

import com.erumpay.notification.domain.entity.Notification;
import com.erumpay.notification.domain.entity.NotificationPreference;
import com.erumpay.notification.domain.enums.NotificationChannel;
import com.erumpay.notification.domain.enums.NotificationType;
import com.erumpay.notification.dto.NotificationEventMessage;
import com.erumpay.notification.event.NotificationPushRequestedEvent;
import com.erumpay.notification.kafka.exception.InvalidNotificationEventException;
import com.erumpay.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NotificationInAppEventServiceTest {

    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final NotificationPreferenceService notificationPreferenceService = mock(NotificationPreferenceService.class);
    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
    private final NotificationInAppEventService notificationInAppEventService =
            new NotificationInAppEventService(
                    notificationRepository,
                    notificationPreferenceService,
                    applicationEventPublisher
            );

    @BeforeEach
    void setUp() {
        reset(notificationRepository, notificationPreferenceService, applicationEventPublisher);
    }

    @Test
    void createsInAppNotificationWhenPreferenceIsEnabled() {
        NotificationEventMessage event = paymentCompletedEvent("evt_in_app_001");
        when(notificationRepository.existsByEventId("evt_in_app_001")).thenReturn(false);
        when(notificationPreferenceService.getOrCreateDefault(101L))
                .thenReturn(NotificationPreference.defaultFor(101L));
        when(notificationRepository.save(org.mockito.ArgumentMatchers.any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationInAppEventService.createInAppNotification(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();

        assertThat(saved.getEventId()).isEqualTo("evt_in_app_001");
        assertThat(saved.getUserId()).isEqualTo(101L);
        assertThat(saved.getType()).isEqualTo(NotificationType.PAYMENT_COMPLETED);
        assertThat(saved.getPaymentId()).isEqualTo(90001L);
        assertThat(saved.getChannel()).isEqualTo(NotificationChannel.IN_APP);
        assertThat(saved.getIsRead()).isFalse();
        verify(applicationEventPublisher).publishEvent(org.mockito.ArgumentMatchers.any(NotificationPushRequestedEvent.class));
    }

    @Test
    void skipsDuplicateEventIdWithoutFailure() {
        NotificationEventMessage event = paymentCompletedEvent("evt_duplicate_001");
        when(notificationRepository.existsByEventId("evt_duplicate_001")).thenReturn(true);

        notificationInAppEventService.createInAppNotification(event);

        verifyNoInteractions(notificationPreferenceService);
        verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(applicationEventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void skipsWhenMappedPreferenceIsDisabled() {
        NotificationEventMessage event = paymentCompletedEvent("evt_disabled_001");
        NotificationPreference preference = NotificationPreference.builder()
                .userId(101L)
                .paymentEnabled(false)
                .build();
        when(notificationRepository.existsByEventId("evt_disabled_001")).thenReturn(false);
        when(notificationPreferenceService.getOrCreateDefault(101L)).thenReturn(preference);

        notificationInAppEventService.createInAppNotification(event);

        verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(applicationEventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsNotificationEventWithoutTitle() {
        NotificationEventMessage event = new NotificationEventMessage(
                "evt_invalid_title_001",
                NotificationType.CARD_REGISTERED,
                101L,
                null,
                "Card registered.",
                null,
                LocalDateTime.of(2026, 5, 26, 10, 0, 5)
        );
        when(notificationRepository.existsByEventId("evt_invalid_title_001")).thenReturn(false);

        assertThatThrownBy(() -> notificationInAppEventService.createInAppNotification(event))
                .isInstanceOf(InvalidNotificationEventException.class)
                .hasMessage("Kafka notification title is required.");
    }

    @Test
    void rejectsPaymentCompletedWithoutPaymentId() {
        NotificationEventMessage event = new NotificationEventMessage(
                "evt_invalid_payment_001",
                NotificationType.PAYMENT_COMPLETED,
                101L,
                "Payment completed.",
                "The payment was completed.",
                null,
                LocalDateTime.of(2026, 5, 26, 10, 0, 5)
        );
        when(notificationRepository.existsByEventId("evt_invalid_payment_001")).thenReturn(false);

        assertThatThrownBy(() -> notificationInAppEventService.createInAppNotification(event))
                .isInstanceOf(InvalidNotificationEventException.class)
                .hasMessage("Kafka notification paymentId is required.");
    }

    private NotificationEventMessage paymentCompletedEvent(String eventId) {
        return new NotificationEventMessage(
                eventId,
                NotificationType.PAYMENT_COMPLETED,
                101L,
                "Payment completed.",
                "The payment was completed.",
                90001L,
                LocalDateTime.of(2026, 5, 26, 10, 0, 5)
        );
    }
}
