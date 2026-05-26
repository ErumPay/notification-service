package com.erumpay.notification.client.push.dto;

import com.erumpay.notification.client.push.enums.PushSendFailureType;

public record PushSendResult(
        boolean success,
        String messageId,
        PushSendFailureType failureType,
        String failureMessage
) {

    public static PushSendResult success(String messageId) {
        return new PushSendResult(true, messageId, null, null);
    }

    public static PushSendResult failure(PushSendFailureType failureType, String failureMessage) {
        return new PushSendResult(false, null, failureType, failureMessage);
    }
}
