package com.erumpay.notification.kafka.service;

import com.erumpay.notification.dto.NotificationEventMessage;
import com.erumpay.notification.kafka.exception.InvalidNotificationEventException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class NotificationEventParser {

    private final ObjectMapper objectMapper;

    public NotificationEventMessage parse(String payload) {
        if (!StringUtils.hasText(payload)) {
            throw new InvalidNotificationEventException("Kafka notification event payload is empty.");
        }

        NotificationEventMessage event = read(payload);
        validate(event);
        return event;
    }

    private NotificationEventMessage read(String payload) {
        try {
            return objectMapper.readValue(payload, NotificationEventMessage.class);
        } catch (JsonProcessingException exception) {
            throw new InvalidNotificationEventException("Kafka notification event payload is invalid.", exception);
        }
    }

    private void validate(NotificationEventMessage event) {
        if (event == null) {
            throw new InvalidNotificationEventException("Kafka notification event payload is null.");
        }
        if (!StringUtils.hasText(event.eventId())) {
            throw new InvalidNotificationEventException("Kafka notification eventId is required.");
        }
        if (event.eventType() == null) {
            throw new InvalidNotificationEventException("Kafka notification eventType is required.");
        }
        if (event.userId() == null) {
            throw new InvalidNotificationEventException("Kafka notification userId is required.");
        }
        if (event.occurredAt() == null) {
            throw new InvalidNotificationEventException("Kafka notification occurredAt is required.");
        }
    }
}
