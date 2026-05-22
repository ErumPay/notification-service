package com.erumpay.notification.kafka.service;

import com.erumpay.notification.domain.enums.NotificationType;
import com.erumpay.notification.dto.NotificationEventMessage;
import com.erumpay.notification.kafka.exception.InvalidNotificationEventException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationEventParserTest {

    private final NotificationEventParser notificationEventParser =
            new NotificationEventParser(new ObjectMapper().findAndRegisterModules());

    @Test
    void parsesKnownNotificationEvent() {
        NotificationEventMessage event = notificationEventParser.parse("""
                {
                  "eventId": "evt_20260513_0001",
                  "eventType": "PAYMENT_COMPLETED",
                  "userId": 101,
                  "title": "Payment completed.",
                  "content": "The payment was completed.",
                  "paymentId": 90001,
                  "occurredAt": "2026-05-13T10:00:05"
                }
                """);

        assertThat(event.eventId()).isEqualTo("evt_20260513_0001");
        assertThat(event.eventType()).isEqualTo(NotificationType.PAYMENT_COMPLETED);
        assertThat(event.userId()).isEqualTo(101L);
    }

    @Test
    void rejectsUnknownEventType() {
        assertThatThrownBy(() -> notificationEventParser.parse("""
                {
                  "eventId": "evt_20260513_0002",
                  "eventType": "PAYMENT_UNKNOWN",
                  "userId": 101,
                  "occurredAt": "2026-05-13T10:00:05"
                }
                """))
                .isInstanceOf(InvalidNotificationEventException.class)
                .hasMessage("Kafka notification event payload is invalid.");
    }

    @Test
    void rejectsEventWithoutOccurredAt() {
        assertThatThrownBy(() -> notificationEventParser.parse("""
                {
                  "eventId": "evt_20260513_0003",
                  "eventType": "CARD_REGISTERED",
                  "userId": 101
                }
                """))
                .isInstanceOf(InvalidNotificationEventException.class)
                .hasMessage("Kafka notification occurredAt is required.");
    }
}
