package com.erumpay.notification.kafka.service;

import com.erumpay.notification.domain.enums.NotificationType;
import com.erumpay.notification.dto.NotificationEventMessage;
import java.time.LocalDateTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatCode;

class NotificationEventRouterTest {

    private final NotificationEventRouter notificationEventRouter = new NotificationEventRouter();

    @ParameterizedTest
    @EnumSource(NotificationType.class)
    void routesKnownEventTypesWithoutException(NotificationType notificationType) {
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
    }
}
