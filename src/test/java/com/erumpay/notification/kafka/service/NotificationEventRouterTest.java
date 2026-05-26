package com.erumpay.notification.kafka.service;

import com.erumpay.notification.domain.enums.NotificationType;
import com.erumpay.notification.dto.NotificationEventMessage;
import com.erumpay.notification.service.NotificationInAppEventService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class NotificationEventRouterTest {

    private final NotificationInAppEventService notificationInAppEventService = mock(NotificationInAppEventService.class);
    private final NotificationEventRouter notificationEventRouter =
            new NotificationEventRouter(notificationInAppEventService);

    @BeforeEach
    void setUp() {
        reset(notificationInAppEventService);
    }

    @ParameterizedTest
    @EnumSource(
            value = NotificationType.class,
            names = "USER_WITHDRAWN",
            mode = EnumSource.Mode.EXCLUDE
    )
    void routesNotificationEventTypesToInAppEventService(NotificationType notificationType) {
        NotificationEventMessage event = new NotificationEventMessage(
                "evt_router_" + notificationType.name(),
                notificationType,
                101L,
                "Notification title",
                "Notification content",
                90001L,
                LocalDateTime.of(2026, 5, 26, 10, 0, 5)
        );

        assertThatCode(() -> notificationEventRouter.route(event)).doesNotThrowAnyException();
        verify(notificationInAppEventService).createInAppNotification(event);
    }

    @ParameterizedTest
    @EnumSource(
            value = NotificationType.class,
            names = "USER_WITHDRAWN",
            mode = EnumSource.Mode.INCLUDE
    )
    void routesUserWithdrawalWithoutCreatingInAppNotification(NotificationType notificationType) {
        NotificationEventMessage event = new NotificationEventMessage(
                "evt_router_" + notificationType.name(),
                notificationType,
                101L,
                null,
                null,
                null,
                LocalDateTime.of(2026, 5, 26, 10, 0, 5)
        );

        assertThatCode(() -> notificationEventRouter.route(event)).doesNotThrowAnyException();
        verifyNoInteractions(notificationInAppEventService);
    }
}
