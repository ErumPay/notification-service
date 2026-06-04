package com.erumpay.notification.domain.entity;

import com.erumpay.notification.domain.enums.NotificationChannel;
import com.erumpay.notification.domain.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "notifications", uniqueConstraints = {
        @UniqueConstraint(name = "uk_notifications_event_id", columnNames = "event_id")
}, indexes = {
        @Index(name = "idx_notifications_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_notifications_user_read", columnList = "user_id, is_read"),
        @Index(name = "idx_notifications_payment", columnList = "payment_id"),
        @Index(name = "idx_notifications_type", columnList = "type")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "ENUM('PAYMENT_COMPLETED','PAYMENT_CANCELED','CARD_REGISTERED','CARD_DELETED','REMOTE_REQUESTED','REMOTE_APPROVED','REMOTE_REJECTED','REMOTE_COMPLETED','DUTCHPAY_INVITED','DUTCHPAY_CONFIRMED','DUTCHPAY_1ST_TIMEDOUT','DUTCHPAY_2ST_TIMEDOUT','DUTCHPAY_COMPLETED','AUTH_FRIEND')")
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, columnDefinition = "ENUM('PUSH','IN_APP')")
    private NotificationChannel channel = NotificationChannel.IN_APP;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static Notification inApp(
            String eventId,
            Long userId,
            NotificationType type,
            String title,
            String content,
            Long paymentId
    ) {
        return Notification.builder()
                .eventId(eventId)
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .paymentId(paymentId)
                .channel(NotificationChannel.IN_APP)
                .build();
    }

    public void markAsRead(LocalDateTime readAt) {
        if (Boolean.TRUE.equals(this.isRead)) {
            return;
        }

        this.isRead = true;
        this.readAt = readAt;
    }
}
