package com.erumpay.notification.controller;

import com.erumpay.notification.domain.entity.NotificationPreference;
import com.erumpay.notification.dto.NotificationPreferenceResponse;
import com.erumpay.notification.service.NotificationPreferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InternalNotificationPreferenceControllerTest {

    private final NotificationPreferenceService notificationPreferenceService =
            mock(NotificationPreferenceService.class);
    private final InternalNotificationPreferenceController controller =
            new InternalNotificationPreferenceController(notificationPreferenceService);

    @Test
    void returnsCreatedWhenDefaultPreferenceIsCreated() {
        NotificationPreference preference = NotificationPreference.defaultFor(101L);
        when(notificationPreferenceService.existsByUserId(101L)).thenReturn(false);
        when(notificationPreferenceService.getOrCreateDefault(101L)).thenReturn(preference);

        ResponseEntity<NotificationPreferenceResponse> response = controller.createDefaultPreference(101L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().userId()).isEqualTo(101L);
        assertThat(response.getBody().pushEnabled()).isTrue();
    }

    @Test
    void returnsOkWhenDefaultPreferenceAlreadyExists() {
        NotificationPreference preference = NotificationPreference.defaultFor(101L);
        when(notificationPreferenceService.existsByUserId(101L)).thenReturn(true);
        when(notificationPreferenceService.getOrCreateDefault(101L)).thenReturn(preference);

        ResponseEntity<NotificationPreferenceResponse> response = controller.createDefaultPreference(101L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
