package com.erumpay.notification.client.auth.dto;

public record AuthDeviceTokenDeactivateRequest(
        String fcmToken,
        DeviceOs deviceOs,
        DeviceTokenInactiveReason reason
) {
}
