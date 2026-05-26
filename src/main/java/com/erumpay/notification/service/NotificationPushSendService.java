package com.erumpay.notification.service;

import com.erumpay.notification.client.auth.AuthDeviceTokenGateway;
import com.erumpay.notification.client.auth.dto.AuthActiveDeviceTokenResponse;
import com.erumpay.notification.client.auth.dto.AuthDeviceTokenDeactivateRequest;
import com.erumpay.notification.client.auth.dto.AuthDeviceTokenResponse;
import com.erumpay.notification.client.push.PushClient;
import com.erumpay.notification.client.push.dto.PushSendRequest;
import com.erumpay.notification.client.push.dto.PushSendResult;
import com.erumpay.notification.client.push.enums.PushSendFailureType;
import com.erumpay.notification.config.AsyncConfig;
import com.erumpay.notification.event.NotificationPushRequestedEvent;
import java.time.Clock;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPushSendService {

    private static final LocalTime NIGHT_BLOCK_START = LocalTime.of(22, 0);
    private static final LocalTime NIGHT_BLOCK_END = LocalTime.of(7, 0);

    private final AuthDeviceTokenGateway authDeviceTokenGateway;
    private final PushClient pushClient;
    private final Clock seoulClock;

    @Async(AsyncConfig.PUSH_TASK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPush(NotificationPushRequestedEvent event) {
        if (!Boolean.TRUE.equals(event.pushEnabled())) {
            log.debug(
                    "Push skipped because push is disabled. notificationId={}, userId={}",
                    event.notificationId(),
                    event.userId()
            );
            return;
        }

        if (Boolean.TRUE.equals(event.nightBlocked()) && isNightBlockedNow()) {
            log.debug(
                    "Push skipped by night block policy. notificationId={}, userId={}",
                    event.notificationId(),
                    event.userId()
            );
            return;
        }

        List<AuthDeviceTokenResponse> tokens = getActiveTokens(event);
        if (tokens.isEmpty()) {
            log.debug(
                    "Push skipped because active FCM token does not exist. notificationId={}, userId={}",
                    event.notificationId(),
                    event.userId()
            );
            return;
        }

        PushSummary summary = sendToTokens(event, tokens);
        if (summary.successCount() > 0) {
            log.info(
                    "Push sent. notificationId={}, userId={}, successCount={}, failureCount={}, invalidTokenCount={}",
                    event.notificationId(),
                    event.userId(),
                    summary.successCount(),
                    summary.failureCount(),
                    summary.invalidTokenCount()
            );
            return;
        }

        log.warn(
                "Push failed for all active tokens. notificationId={}, userId={}, failureCount={}, invalidTokenCount={}",
                event.notificationId(),
                event.userId(),
                summary.failureCount(),
                summary.invalidTokenCount()
        );
    }

    private List<AuthDeviceTokenResponse> getActiveTokens(NotificationPushRequestedEvent event) {
        try {
            AuthActiveDeviceTokenResponse response = authDeviceTokenGateway.getActiveDeviceTokens(event.userId());
            if (response == null || response.tokens() == null) {
                return List.of();
            }
            return response.tokens().stream()
                    .filter(token -> token != null && StringUtils.hasText(token.fcmToken()))
                    .toList();
        } catch (Exception exception) {
            log.warn(
                    "Push skipped because auth-service token lookup failed. notificationId={}, userId={}, reason={}",
                    event.notificationId(),
                    event.userId(),
                    exception.getMessage()
            );
            return List.of();
        }
    }

    private PushSummary sendToTokens(NotificationPushRequestedEvent event, List<AuthDeviceTokenResponse> tokens) {
        int successCount = 0;
        int failureCount = 0;
        int invalidTokenCount = 0;

        for (AuthDeviceTokenResponse token : tokens) {
            try {
                PushSendResult result = pushClient.send(toPushSendRequest(event, token.fcmToken()));
                if (result.success()) {
                    successCount++;
                    continue;
                }

                if (result.failureType() == PushSendFailureType.INVALID_TOKEN) {
                    invalidTokenCount++;
                    deactivateInvalidToken(token.fcmToken(), event);
                    continue;
                }

                failureCount++;
                log.warn(
                        "Push send failed. notificationId={}, userId={}, failureType={}, reason={}",
                        event.notificationId(),
                        event.userId(),
                        result.failureType(),
                        result.failureMessage()
                );
            } catch (Exception exception) {
                failureCount++;
                log.warn(
                        "Push send failed by unexpected exception. notificationId={}, userId={}, reason={}",
                        event.notificationId(),
                        event.userId(),
                        exception.getMessage()
                );
            }
        }

        return new PushSummary(successCount, failureCount, invalidTokenCount);
    }

    private PushSendRequest toPushSendRequest(NotificationPushRequestedEvent event, String fcmToken) {
        Map<String, String> data = new HashMap<>();
        data.put("notificationId", String.valueOf(event.notificationId()));
        data.put("type", event.type().name());
        if (event.paymentId() != null) {
            data.put("paymentId", String.valueOf(event.paymentId()));
        }

        return new PushSendRequest(
                fcmToken,
                event.title(),
                event.content(),
                event.notificationId(),
                event.type(),
                event.paymentId(),
                data
        );
    }

    private void deactivateInvalidToken(String fcmToken, NotificationPushRequestedEvent event) {
        try {
            authDeviceTokenGateway.deactivateInvalidToken(new AuthDeviceTokenDeactivateRequest(fcmToken));
        } catch (Exception exception) {
            log.warn(
                    "Failed to request invalid token deactivation. notificationId={}, userId={}, reason={}",
                    event.notificationId(),
                    event.userId(),
                    exception.getMessage()
            );
        }
    }

    private boolean isNightBlockedNow() {
        LocalTime now = LocalTime.now(seoulClock);
        return !now.isBefore(NIGHT_BLOCK_START) || now.isBefore(NIGHT_BLOCK_END);
    }

    private record PushSummary(
            int successCount,
            int failureCount,
            int invalidTokenCount
    ) {
    }
}
