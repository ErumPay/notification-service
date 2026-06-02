package com.erumpay.notification.kafka.consumer;

import com.erumpay.notification.dto.NotificationEventMessage;
import com.erumpay.notification.kafka.exception.InvalidNotificationEventException;
import com.erumpay.notification.kafka.service.NotificationEventParser;
import com.erumpay.notification.kafka.service.NotificationEventRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationEventParser notificationEventParser;
    private final NotificationEventRouter notificationEventRouter;

    @KafkaListener(
            topics = {
                    "${notification.kafka.topics.auth}",
                    "${notification.kafka.topics.card}",
                    "${notification.kafka.topics.dutch}",
                    "${notification.kafka.topics.payment}",
                    "${notification.kafka.topics.remote-command}",
                    "${notification.kafka.topics.remote-event}"
            },
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record) {
        try {
            NotificationEventMessage event = notificationEventParser.parse(record.value());
            notificationEventRouter.route(event);
            log.info(
                    "Notification Kafka event consumed. eventId={}, correlationId={}, eventType={}, userId={}, topic={}, partition={}, offset={}",
                    event.eventId(),
                    event.correlationId(),
                    event.eventType(),
                    event.userId(),
                    record.topic(),
                    record.partition(),
                    record.offset()
            );
        } catch (InvalidNotificationEventException exception) {
            log.warn(
                    "Invalid notification Kafka event. topic={}, partition={}, offset={}, key={}, reason={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    exception.getMessage()
            );
            throw exception;
        }
    }
}
