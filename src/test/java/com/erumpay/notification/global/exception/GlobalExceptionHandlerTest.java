package com.erumpay.notification.global.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handlesNotificationExceptionWithTeamErrorCode() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/notifications/1/read");
        request.addHeader("X-Correlation-Id", "test-correlation-id");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNotificationException(
                new NotificationException(ErrorCode.NOTIFICATION_NOT_FOUND),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().code()).isEqualTo("NTF-NTF-201");
        assertThat(response.getBody().reason()).isEqualTo("NOTIFICATION_NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("알림 정보를 찾을 수 없습니다.");
        assertThat(response.getBody().details()).isEmpty();
        assertThat(response.getBody().correlationId()).isEqualTo("test-correlation-id");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/notifications/1/read");
    }

    @Test
    void handlesValidationExceptionWithDetails() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/notifications");
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("page", "int");

        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleValidationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("NTF-REQ-001");
        assertThat(response.getBody().reason()).isEqualTo("INVALID_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("잘못된 요청입니다.");
        assertThat(response.getBody().details()).hasSize(1);
        assertThat(response.getBody().details().get(0).field()).isEqualTo("page");
        assertThat(response.getBody().correlationId()).isNull();
    }

    @Test
    void handlesUnknownExceptionAsInternalServerError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/notifications");

        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleException(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("NTF-SYS-900");
        assertThat(response.getBody().reason()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().message()).isEqualTo("알 수 없는 내부 오류가 발생했습니다.");
        assertThat(response.getBody().details()).isEmpty();
    }
}
