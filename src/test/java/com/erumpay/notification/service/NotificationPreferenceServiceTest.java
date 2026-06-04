package com.erumpay.notification.service;

import com.erumpay.notification.domain.entity.NotificationPreference;
import com.erumpay.notification.dto.NotificationPreferenceUpdateRequest;
import com.erumpay.notification.repository.NotificationPreferenceRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationPreferenceServiceTest {

    private final NotificationPreferenceRepository notificationPreferenceRepository =
            mock(NotificationPreferenceRepository.class);
    private final NotificationPreferenceService notificationPreferenceService =
            new NotificationPreferenceService(notificationPreferenceRepository);

    @BeforeEach
    void setUp() {
        reset(notificationPreferenceRepository);
    }

    @Test
    void returnsExistingPreference() {
        NotificationPreference existing = NotificationPreference.defaultFor(101L);
        when(notificationPreferenceRepository.findByUserId(101L)).thenReturn(Optional.of(existing));

        NotificationPreference result = notificationPreferenceService.getOrCreateDefault(101L);

        assertThat(result).isSameAs(existing);
        verify(notificationPreferenceRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createsDefaultPreferenceWhenMissing() {
        NotificationPreference created = NotificationPreference.defaultFor(101L);
        when(notificationPreferenceRepository.findByUserId(101L)).thenReturn(Optional.empty());
        when(notificationPreferenceRepository.save(org.mockito.ArgumentMatchers.any(NotificationPreference.class)))
                .thenReturn(created);

        NotificationPreference result = notificationPreferenceService.getOrCreateDefault(101L);

        assertThat(result.getUserId()).isEqualTo(101L);
        assertThat(result.getPushEnabled()).isTrue();
        assertThat(result.getNightBlocked()).isFalse();
    }

    @Test
    void getsPreferenceWithDefaultFallback() {
        NotificationPreference created = NotificationPreference.defaultFor(101L);
        when(notificationPreferenceRepository.findByUserId(101L)).thenReturn(Optional.empty());
        when(notificationPreferenceRepository.save(org.mockito.ArgumentMatchers.any(NotificationPreference.class)))
                .thenReturn(created);

        NotificationPreference result = notificationPreferenceService.getPreference(101L);

        assertThat(result.getUserId()).isEqualTo(101L);
        assertThat(result.getPushEnabled()).isTrue();
        assertThat(result.getNightBlocked()).isFalse();
    }

    @Test
    void updatesPreference() {
        NotificationPreference existing = NotificationPreference.defaultFor(101L);
        NotificationPreferenceUpdateRequest request = new NotificationPreferenceUpdateRequest(
                true,
                false,
                true,
                true,
                false,
                true,
                true
        );
        when(notificationPreferenceRepository.findByUserId(101L)).thenReturn(Optional.of(existing));

        NotificationPreference result = notificationPreferenceService.updatePreference(101L, request);

        assertThat(result.getPushEnabled()).isTrue();
        assertThat(result.getCardEnabled()).isFalse();
        assertThat(result.getPaymentEnabled()).isTrue();
        assertThat(result.getDutchpayEnabled()).isTrue();
        assertThat(result.getRemoteEnabled()).isFalse();
        assertThat(result.getFriendEnabled()).isTrue();
        assertThat(result.getNightBlocked()).isTrue();
        verify(notificationPreferenceRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createsDefaultPreferenceBeforeUpdateWhenMissing() {
        NotificationPreference created = NotificationPreference.defaultFor(101L);
        NotificationPreferenceUpdateRequest request = new NotificationPreferenceUpdateRequest(
                false,
                false,
                false,
                true,
                true,
                false,
                true
        );
        when(notificationPreferenceRepository.findByUserId(101L)).thenReturn(Optional.empty());
        when(notificationPreferenceRepository.save(org.mockito.ArgumentMatchers.any(NotificationPreference.class)))
                .thenReturn(created);

        NotificationPreference result = notificationPreferenceService.updatePreference(101L, request);

        assertThat(result.getPushEnabled()).isFalse();
        assertThat(result.getCardEnabled()).isFalse();
        assertThat(result.getPaymentEnabled()).isFalse();
        assertThat(result.getDutchpayEnabled()).isTrue();
        assertThat(result.getRemoteEnabled()).isTrue();
        assertThat(result.getFriendEnabled()).isFalse();
        assertThat(result.getNightBlocked()).isTrue();
    }
}
