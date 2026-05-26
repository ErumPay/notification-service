package com.erumpay.notification.service;

import com.erumpay.notification.domain.entity.NotificationPreference;
import com.erumpay.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    @Transactional(readOnly = true)
    public boolean existsByUserId(Long userId) {
        return notificationPreferenceRepository.existsByUserId(userId);
    }

    @Transactional
    public NotificationPreference getOrCreateDefault(Long userId) {
        return notificationPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> notificationPreferenceRepository.save(NotificationPreference.defaultFor(userId)));
    }
}
