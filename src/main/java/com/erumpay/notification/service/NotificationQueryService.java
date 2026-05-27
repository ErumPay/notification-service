package com.erumpay.notification.service;

import com.erumpay.notification.domain.entity.Notification;
import com.erumpay.notification.domain.enums.NotificationChannel;
import com.erumpay.notification.dto.NotificationResponse;
import com.erumpay.notification.dto.PageResponse;
import com.erumpay.notification.global.exception.ErrorCode;
import com.erumpay.notification.global.exception.NotificationException;
import com.erumpay.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(
            Long userId,
            int page,
            int size,
            Boolean isRead
    ) {
        Pageable pageable = createPageable(page, size);
        Page<Notification> notifications = isRead == null
                ? notificationRepository.findByUserIdAndChannel(userId, NotificationChannel.IN_APP, pageable)
                : notificationRepository.findByUserIdAndChannelAndIsRead(
                        userId,
                        NotificationChannel.IN_APP,
                        isRead,
                        pageable
                );

        return PageResponse.from(notifications.map(NotificationResponse::from));
    }

    private Pageable createPageable(int page, int size) {
        if (page < 0) {
            throw new NotificationException(ErrorCode.INVALID_REQUEST, "page must be greater than or equal to 0.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new NotificationException(ErrorCode.INVALID_REQUEST, "size must be between 1 and 100.");
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
