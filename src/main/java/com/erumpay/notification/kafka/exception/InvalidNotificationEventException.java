package com.erumpay.notification.kafka.exception;

import com.erumpay.notification.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidNotificationEventException extends RuntimeException {

    private final ErrorCode errorCode;

    public InvalidNotificationEventException(String message) {
        super(message);
        this.errorCode = ErrorCode.KAFKA_EVENT_PROCESS_FAILED;
    }

    public InvalidNotificationEventException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.KAFKA_EVENT_PROCESS_FAILED;
    }
}
