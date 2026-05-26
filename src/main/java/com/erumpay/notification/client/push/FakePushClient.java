package com.erumpay.notification.client.push;

import com.erumpay.notification.client.push.dto.PushSendRequest;
import com.erumpay.notification.client.push.dto.PushSendResult;
import com.erumpay.notification.client.push.enums.PushSendFailureType;
import org.springframework.stereotype.Component;

@Component
public class FakePushClient implements PushClient {

    @Override
    public PushSendResult send(PushSendRequest request) {
        String token = request.fcmToken();
        if (token != null && token.contains("invalid")) {
            return PushSendResult.failure(PushSendFailureType.INVALID_TOKEN, "Fake invalid token.");
        }
        if (token != null && token.contains("retryable")) {
            return PushSendResult.failure(PushSendFailureType.RETRYABLE, "Fake retryable push failure.");
        }
        if (token != null && token.contains("fail")) {
            return PushSendResult.failure(PushSendFailureType.PERMANENT, "Fake permanent push failure.");
        }
        return PushSendResult.success("fake-message-id");
    }
}
