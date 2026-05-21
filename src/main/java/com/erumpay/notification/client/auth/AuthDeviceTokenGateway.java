package com.erumpay.notification.client.auth;

import com.erumpay.notification.client.auth.dto.AuthActiveDeviceTokenResponse;
import com.erumpay.notification.client.auth.dto.AuthDeviceTokenDeactivateRequest;
import com.erumpay.notification.global.exception.ErrorCode;
import com.erumpay.notification.global.exception.NotificationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthDeviceTokenGateway {

    private static final String AUTH_DEVICE_TOKEN_CLIENT = "authDeviceTokenClient";

    private final AuthDeviceTokenClient authDeviceTokenClient;

    @Retry(name = AUTH_DEVICE_TOKEN_CLIENT)
    @CircuitBreaker(name = AUTH_DEVICE_TOKEN_CLIENT, fallbackMethod = "getActiveDeviceTokensFallback")
    public AuthActiveDeviceTokenResponse getActiveDeviceTokens(Long userId) {
        return authDeviceTokenClient.getActiveDeviceTokens(userId);
    }

    @Retry(name = AUTH_DEVICE_TOKEN_CLIENT)
    @CircuitBreaker(name = AUTH_DEVICE_TOKEN_CLIENT, fallbackMethod = "deactivateInvalidTokenFallback")
    public void deactivateInvalidToken(AuthDeviceTokenDeactivateRequest request) {
        authDeviceTokenClient.deactivateInvalidToken(request);
    }

    private AuthActiveDeviceTokenResponse getActiveDeviceTokensFallback(Long userId, Throwable cause) {
        throw new NotificationException(
                ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
                "Failed to retrieve active device tokens from auth-service."
        );
    }

    private void deactivateInvalidTokenFallback(AuthDeviceTokenDeactivateRequest request, Throwable cause) {
        throw new NotificationException(
                ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
                "Failed to deactivate invalid device token through auth-service."
        );
    }
}
