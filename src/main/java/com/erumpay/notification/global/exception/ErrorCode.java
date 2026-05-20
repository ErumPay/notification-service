package com.erumpay.notification.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid request."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", "Notification was not found."),
    NOTIFICATION_PREFERENCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "NOTIFICATION_PREFERENCE_NOT_FOUND",
            "Notification preference was not found."
    ),
    DUPLICATE_EVENT_ID(HttpStatus.CONFLICT, "DUPLICATE_EVENT_ID", "Notification event already exists."),
    NOTIFICATION_SAVE_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "NOTIFICATION_SAVE_FAILED",
            "Failed to save notification."
    ),
    EXTERNAL_SERVICE_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "EXTERNAL_SERVICE_TIMEOUT", "External service timed out."),
    EXTERNAL_SERVICE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "EXTERNAL_SERVICE_UNAVAILABLE",
            "External service is unavailable."
    ),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Internal server error.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
