package com.erumpay.notification.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "notification_preferences", uniqueConstraints = {
        @UniqueConstraint(name = "uk_notification_preferences_user", columnNames = "user_id")
})
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long preferenceId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder.Default
    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = true;

    @Builder.Default
    @Column(name = "card_enabled", nullable = false)
    private Boolean cardEnabled = true;

    @Builder.Default
    @Column(name = "payment_enabled", nullable = false)
    private Boolean paymentEnabled = true;

    @Builder.Default
    @Column(name = "dutchpay_enabled", nullable = false)
    private Boolean dutchpayEnabled = true;

    @Builder.Default
    @Column(name = "remote_enabled", nullable = false)
    private Boolean remoteEnabled = true;

    @Builder.Default
    @Column(name = "friend_enabled", nullable = false)
    private Boolean friendEnabled = true;

    @Builder.Default
    @Column(name = "night_blocked", nullable = false)
    private Boolean nightBlocked = false;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void update(
            Boolean pushEnabled,
            Boolean cardEnabled,
            Boolean paymentEnabled,
            Boolean dutchpayEnabled,
            Boolean remoteEnabled,
            Boolean friendEnabled,
            Boolean nightBlocked
    ) {
        this.pushEnabled = pushEnabled;
        this.cardEnabled = cardEnabled;
        this.paymentEnabled = paymentEnabled;
        this.dutchpayEnabled = dutchpayEnabled;
        this.remoteEnabled = remoteEnabled;
        this.friendEnabled = friendEnabled;
        this.nightBlocked = nightBlocked;
    }
}
