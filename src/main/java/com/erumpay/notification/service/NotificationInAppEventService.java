package com.erumpay.notification.service;

import com.erumpay.notification.domain.entity.Notification;
import com.erumpay.notification.domain.entity.NotificationPreference;
import com.erumpay.notification.domain.enums.NotificationType;
import com.erumpay.notification.dto.NotificationEventMessage;
import com.erumpay.notification.event.NotificationPushRequestedEvent;
import com.erumpay.notification.kafka.exception.InvalidNotificationEventException;
import com.erumpay.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationInAppEventService {

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int CONTENT_MAX_LENGTH = 500;

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceService notificationPreferenceService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void createInAppNotification(NotificationEventMessage event) {
        if (notificationRepository.existsByEventId(event.eventId())) {
            log.info("Duplicate notification event skipped. eventId={}, userId={}", event.eventId(), event.userId());
            return;
        }

        validateNotificationEvent(event);

        NotificationPreference preference = notificationPreferenceService.getOrCreateDefault(event.userId());
        if (!isEnabled(preference, event.eventType())) {
            log.info(
                    "Notification event skipped by user preference. eventId={}, eventType={}, userId={}",
                    event.eventId(),
                    event.eventType(),
                    event.userId()
            );
            return;
        }

        Notification notification = notificationRepository.save(Notification.inApp(
                event.eventId(),
                event.userId(),
                event.eventType(),
                event.title(),
                event.content(),
                event.paymentId()
        ));
        applicationEventPublisher.publishEvent(new NotificationPushRequestedEvent(
                notification.getNotificationId(),
                notification.getUserId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getPaymentId(),
                preference.getPushEnabled(),
                preference.getNightBlocked()
        ));
    }

    private void validateNotificationEvent(NotificationEventMessage event) {
        if (!StringUtils.hasText(event.title())) {
            throw new InvalidNotificationEventException("Kafka notification title is required.");
        }
        if (!StringUtils.hasText(event.content())) {
            throw new InvalidNotificationEventException("Kafka notification content is required.");
        }
        if (event.title().length() > TITLE_MAX_LENGTH) {
            throw new InvalidNotificationEventException("Kafka notification title is too long.");
        }
        if (event.content().length() > CONTENT_MAX_LENGTH) {
            throw new InvalidNotificationEventException("Kafka notification content is too long.");
        }
        if (requiresPaymentId(event.eventType()) && event.paymentId() == null) {
            throw new InvalidNotificationEventException("Kafka notification paymentId is required.");
        }
    }

    private boolean requiresPaymentId(NotificationType eventType) {
        return eventType == NotificationType.PAYMENT_COMPLETED || eventType == NotificationType.PAYMENT_CANCELED;
    }

    private boolean isEnabled(NotificationPreference preference, NotificationType eventType) {
        return switch (eventType) {
            case PAYMENT_COMPLETED, PAYMENT_CANCELED -> Boolean.TRUE.equals(preference.getPaymentEnabled());
            case CARD_REGISTERED, CARD_DELETED -> Boolean.TRUE.equals(preference.getCardEnabled());
            case REMOTE_REQUESTED, REMOTE_APPROVED, REMOTE_REJECTED, REMOTE_COMPLETED ->
                    Boolean.TRUE.equals(preference.getRemoteEnabled());
            case DUTCHPAY_INVITED, DUTCHPAY_CONFIRMED, DUTCHPAY_1ST_TIMEDOUT, DUTCHPAY_2ST_TIMEDOUT,
                    DUTCHPAY_COMPLETED -> Boolean.TRUE.equals(preference.getDutchpayEnabled());
            case AUTH_FRIEND -> Boolean.TRUE.equals(preference.getFriendEnabled());
            case USER_WITHDRAWN -> false;
        };
    }
}
