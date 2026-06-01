package com.erumpay.notification.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "NTF-REQ-001", "INVALID_REQUEST", "잘못된 요청입니다."),
    AUTHORIZATION_REQUIRED(HttpStatus.UNAUTHORIZED, "NTF-AUTH-100", "AUTHORIZATION_REQUIRED", "인증 정보가 필요합니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NTF-NTF-201", "NOTIFICATION_NOT_FOUND", "알림 정보를 찾을 수 없습니다."),
    NOTIFICATION_PREFERENCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "NTF-NTF-202",
            "NOTIFICATION_PREFERENCE_NOT_FOUND",
            "알림 수신 설정을 찾을 수 없습니다."
    ),
    INVALID_NOTIFICATION_STATE(
            HttpStatus.CONFLICT,
            "NTF-NTF-203",
            "INVALID_NOTIFICATION_STATE",
            "현재 알림 상태에서는 요청한 작업을 처리할 수 없습니다."
    ),
    DUPLICATE_EVENT_ID(HttpStatus.CONFLICT, "NTF-NTF-301", "DUPLICATE_EVENT_ID", "이미 처리된 알림 이벤트입니다."),
    FCM_SEND_FAILED(HttpStatus.BAD_GATEWAY, "NTF-FCM-400", "FCM_SEND_FAILED", "FCM 알림 발송에 실패했습니다."),
    FCM_TOKEN_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "NTF-FCM-401", "FCM_TOKEN_INVALID", "유효하지 않은 FCM 토큰입니다."),
    FCM_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "NTF-FCM-402", "FCM_TIMEOUT", "FCM 응답 시간이 초과되었습니다."),
    AUTH_SERVICE_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "NTF-AUTH-400",
            "AUTH_SERVICE_UNAVAILABLE",
            "인증 서비스 연동에 실패했습니다."
    ),
    KAFKA_EVENT_PROCESS_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "NTF-KFK-500",
            "KAFKA_EVENT_PROCESS_FAILED",
            "알림 이벤트 처리에 실패했습니다."
    ),
    KAFKA_EVENT_PUBLISH_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "NTF-KFK-501",
            "KAFKA_EVENT_PUBLISH_FAILED",
            "알림 이벤트 발행에 실패했습니다."
    ),
    NOTIFICATION_SAVE_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "NTF-DB-901",
            "NOTIFICATION_SAVE_FAILED",
            "알림 저장에 실패했습니다."
    ),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "NTF-SYS-900", "INTERNAL_SERVER_ERROR", "알 수 없는 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String reason;
    private final String message;
}
