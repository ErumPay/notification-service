package com.erumpay.notification.kafka.service;

import com.erumpay.notification.dto.NotificationEventMessage;
import com.erumpay.notification.service.NotificationInAppEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventRouter {

    private final NotificationInAppEventService notificationInAppEventService;

    public void route(NotificationEventMessage event) {
        switch (event.eventType()) {
            case PAYMENT_COMPLETED, PAYMENT_CANCELED, CARD_REGISTERED, CARD_DELETED, REMOTE_REQUESTED,
                    REMOTE_APPROVED, REMOTE_REJECTED, REMOTE_COMPLETED, DUTCHPAY_INVITED, DUTCHPAY_CONFIRMED,
                    DUTCHPAY_1ST_TIMEDOUT, DUTCHPAY_2ST_TIMEDOUT, DUTCHPAY_COMPLETED, AUTH_FRIEND ->
                    routeNotificationEvent(event);
            case USER_WITHDRAWN -> routeUserWithdrawalEvent(event);
        }
    }

    private void routeNotificationEvent(NotificationEventMessage event) {
        notificationInAppEventService.createInAppNotification(event);
        log.debug(
                "Notification Kafka event routed for notification handling. eventId={}, eventType={}, userId={}",
                event.eventId(),
                event.eventType(),
                event.userId()
        );
    }

    private void routeUserWithdrawalEvent(NotificationEventMessage event) {
        log.debug(
                "Notification Kafka event routed for user withdrawal handling. eventId={}, userId={}",
                event.eventId(),
                event.userId()
        );
    }
}
