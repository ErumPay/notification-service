package com.erumpay.notification.client.auth.dto;

public record AuthDeviceTokenResponse(
        String fcmToken,
        DeviceOs deviceOs
) {
}
