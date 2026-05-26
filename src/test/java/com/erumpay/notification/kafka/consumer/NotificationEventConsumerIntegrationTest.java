package com.erumpay.notification.kafka.consumer;

import com.erumpay.notification.dto.NotificationEventMessage;
import com.erumpay.notification.kafka.config.NotificationKafkaConfig;
import com.erumpay.notification.kafka.service.NotificationEventParser;
import com.erumpay.notification.kafka.service.NotificationEventRouter;
import java.time.Duration;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "auth.event",
                "auth.event.dlt",
                "card.event",
                "card.event.dlt",
                "dutch.event",
                "dutch.event.dlt",
                "payment.event",
                "payment.event.dlt",
                "remote.command",
                "remote.command.dlt",
                "remote.event",
                "remote.event.dlt"
        },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@SpringBootTest(
        classes = NotificationEventConsumerIntegrationTest.TestApplication.class,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
                "spring.kafka.consumer.group-id=notification-service-test",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "spring.kafka.consumer.enable-auto-commit=false",
                "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.listener.ack-mode=record",
                "notification.kafka.topics.auth=auth.event",
                "notification.kafka.topics.card=card.event",
                "notification.kafka.topics.dutch=dutch.event",
                "notification.kafka.topics.payment=payment.event",
                "notification.kafka.topics.remote-command=remote.command",
                "notification.kafka.topics.remote-event=remote.event"
        }
)
class NotificationEventConsumerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private NotificationEventRouter notificationEventRouter;

    @BeforeEach
    void setUp() {
        reset(notificationEventRouter);
    }

    @Test
    void consumesPaymentEventAndRoutesIt() {
        kafkaTemplate.send("payment.event", "101", """
                {
                  "eventId": "evt_consumer_001",
                  "eventType": "PAYMENT_COMPLETED",
                  "userId": 101,
                  "title": "Payment completed.",
                  "content": "The payment was completed.",
                  "paymentId": 90001,
                  "occurredAt": "2026-05-26T10:00:05"
                }
                """);

        verify(notificationEventRouter, timeout(5_000))
                .route(argThat(eventWithId("evt_consumer_001")));
    }

    @Test
    void publishesInvalidEventToDeadLetterTopic() {
        try (Consumer<String, String> dltConsumer = createDltConsumer()) {
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(dltConsumer, "payment.event.dlt");

            kafkaTemplate.send("payment.event", "101", """
                    {
                      "eventId": "evt_consumer_invalid_001",
                      "eventType": "PAYMENT_UNKNOWN",
                      "userId": 101,
                      "occurredAt": "2026-05-26T10:00:05"
                    }
                    """);

            ConsumerRecord<String, String> deadLetterRecord =
                    KafkaTestUtils.getSingleRecord(dltConsumer, "payment.event.dlt", Duration.ofSeconds(10));

            assertThat(deadLetterRecord.value()).contains("PAYMENT_UNKNOWN");
            assertThat(deadLetterRecord.key()).isEqualTo("101");
        }
    }

    private Consumer<String, String> createDltConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "notification-service-dlt-test",
                "false",
                embeddedKafkaBroker
        );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new StringDeserializer()
        ).createConsumer();
    }

    private ArgumentMatcher<NotificationEventMessage> eventWithId(String eventId) {
        return event -> event != null && eventId.equals(event.eventId());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            NotificationKafkaConfig.class,
            NotificationEventConsumer.class,
            NotificationEventParser.class
    })
    static class TestApplication {

        @Bean
        NotificationEventRouter notificationEventRouter() {
            return mock(NotificationEventRouter.class);
        }
    }
}
