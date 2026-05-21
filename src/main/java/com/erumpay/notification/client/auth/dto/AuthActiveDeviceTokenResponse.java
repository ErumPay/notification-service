package com.erumpay.notification.client.auth.dto;

import java.util.List;

public record AuthActiveDeviceTokenResponse(
        List<AuthDeviceTokenResponse> tokens
) {
}
