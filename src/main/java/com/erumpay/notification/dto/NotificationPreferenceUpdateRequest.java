package com.erumpay.notification.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationPreferenceUpdateRequest(
        @NotNull Boolean pushEnabled,
        @NotNull Boolean cardEnabled,
        @NotNull Boolean paymentEnabled,
        @NotNull Boolean dutchpayEnabled,
        @NotNull Boolean remoteEnabled,
        @NotNull Boolean friendEnabled,
        @NotNull Boolean nightBlocked
) {
}
