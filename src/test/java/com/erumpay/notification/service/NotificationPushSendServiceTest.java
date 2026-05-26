package com.erumpay.notification.service;

import com.erumpay.notification.client.auth.AuthDeviceTokenGateway;
import com.erumpay.notification.client.auth.dto.AuthActiveDeviceTokenResponse;
import com.erumpay.notification.client.auth.dto.AuthDeviceTokenDeactivateRequest;
import com.erumpay.notification.client.auth.dto.AuthDeviceTokenResponse;
import com.erumpay.notification.client.auth.dto.DeviceOs;
import com.erumpay.notification.client.push.PushClient;
import com.erumpay.notification.client.push.dto.PushSendRequest;
import com.erumpay.notification.client.push.dto.PushSendResult;
import com.erumpay.notification.client.push.enums.PushSendFailureType;
import com.erumpay.notification.domain.enums.NotificationType;
import com.erumpay.notification.event.NotificationPushRequestedEvent;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationPushSendServiceTest {

    private final AuthDeviceTokenGateway authDeviceTokenGateway = mock(AuthDeviceTokenGateway.class);
    private final PushClient pushClient = mock(PushClient.class);
    private Clock clock = Clock.fixed(Instant.parse("2026-05-26T01:00:00Z"), ZoneId.of("Asia/Seoul"));
    private NotificationPushSendService notificationPushSendService =
            new NotificationPushSendService(authDeviceTokenGateway, pushClient, clock);

    @BeforeEach
    void setUp() {
        reset(authDeviceTokenGateway, pushClient);
        clock = Clock.fixed(Instant.parse("2026-05-26T01:00:00Z"), ZoneId.of("Asia/Seoul"));
        notificationPushSendService = new NotificationPushSendService(authDeviceTokenGateway, pushClient, clock);
    }

    @Test
    void skipsWhenPushIsDisabled() {
        notificationPushSendService.sendPush(pushEvent(false, false));

        verify(authDeviceTokenGateway, never()).getActiveDeviceTokens(any());
        verify(pushClient, never()).send(any());
    }

    @Test
    void skipsWhenNightBlocked() {
        Clock nightClock = Clock.fixed(Instant.parse("2026-05-26T14:00:00Z"), ZoneId.of("Asia/Seoul"));
        notificationPushSendService = new NotificationPushSendService(authDeviceTokenGateway, pushClient, nightClock);

        notificationPushSendService.sendPush(pushEvent(true, true));

        verify(authDeviceTokenGateway, never()).getActiveDeviceTokens(any());
        verify(pushClient, never()).send(any());
    }

    @Test
    void sendsToAllActiveTokens() {
        when(authDeviceTokenGateway.getActiveDeviceTokens(101L)).thenReturn(new AuthActiveDeviceTokenResponse(List.of(
                new AuthDeviceTokenResponse("fcm-token-android", DeviceOs.ANDROID),
                new AuthDeviceTokenResponse("fcm-token-ios", DeviceOs.IOS)
        )));
        when(pushClient.send(any(PushSendRequest.class))).thenReturn(PushSendResult.success("message-id"));

        notificationPushSendService.sendPush(pushEvent(true, false));

        verify(pushClient, times(2)).send(any(PushSendRequest.class));
        verify(pushClient).send(argThat(request -> "fcm-token-android".equals(request.fcmToken())));
        verify(pushClient).send(argThat(request -> "fcm-token-ios".equals(request.fcmToken())));
    }

    @Test
    void deactivatesInvalidTokenOnly() {
        when(authDeviceTokenGateway.getActiveDeviceTokens(101L)).thenReturn(new AuthActiveDeviceTokenResponse(List.of(
                new AuthDeviceTokenResponse("valid-token", DeviceOs.ANDROID),
                new AuthDeviceTokenResponse("invalid-token", DeviceOs.IOS)
        )));
        when(pushClient.send(any(PushSendRequest.class))).thenAnswer(invocation -> {
            PushSendRequest request = invocation.getArgument(0);
            if ("invalid-token".equals(request.fcmToken())) {
                return PushSendResult.failure(PushSendFailureType.INVALID_TOKEN, "invalid token");
            }
            return PushSendResult.success("message-id");
        });

        notificationPushSendService.sendPush(pushEvent(true, false));

        verify(authDeviceTokenGateway).deactivateInvalidToken(argThat(
                (AuthDeviceTokenDeactivateRequest request) -> "invalid-token".equals(request.fcmToken())
        ));
    }

    @Test
    void stopsWhenAuthServiceTokenLookupFails() {
        when(authDeviceTokenGateway.getActiveDeviceTokens(101L))
                .thenThrow(new RuntimeException("auth-service timeout"));

        notificationPushSendService.sendPush(pushEvent(true, false));

        verify(pushClient, never()).send(any());
    }

    private NotificationPushRequestedEvent pushEvent(Boolean pushEnabled, Boolean nightBlocked) {
        return new NotificationPushRequestedEvent(
                1L,
                101L,
                NotificationType.PAYMENT_COMPLETED,
                "Payment completed.",
                "The payment was completed.",
                90001L,
                pushEnabled,
                nightBlocked
        );
    }
}
