package com.erumpay.notification.dto;

import com.erumpay.notification.domain.entity.NotificationPreference;

public record NotificationPreferenceResponse(
        Long userId,
        Boolean pushEnabled,
        Boolean cardEnabled,
        Boolean paymentEnabled,
        Boolean dutchpayEnabled,
        Boolean remoteEnabled,
        Boolean friendEnabled,
        Boolean nightBlocked
) {

    public static NotificationPreferenceResponse from(NotificationPreference preference) {
        return new NotificationPreferenceResponse(
                preference.getUserId(),
                preference.getPushEnabled(),
                preference.getCardEnabled(),
                preference.getPaymentEnabled(),
                preference.getDutchpayEnabled(),
                preference.getRemoteEnabled(),
                preference.getFriendEnabled(),
                preference.getNightBlocked()
        );
    }
}
