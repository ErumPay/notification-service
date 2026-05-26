package com.erumpay.notification.client.push.dto;

import com.erumpay.notification.domain.enums.NotificationType;
import java.util.Map;

public record PushSendRequest(
        String fcmToken,
        String title,
        String content,
        Long notificationId,
        NotificationType type,
        Long paymentId,
        Map<String, String> data
) {
}
