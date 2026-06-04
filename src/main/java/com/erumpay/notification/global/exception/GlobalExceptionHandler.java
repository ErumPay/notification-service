package com.erumpay.notification.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ErrorResponse> handleNotificationException(
            NotificationException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(
                        errorCode,
                        exception.getMessage(),
                        request.getRequestURI(),
                        List.of(),
                        correlationId(request)));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingPathVariableException.class,
            MissingRequestHeaderException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationException(Exception exception, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(
                        errorCode,
                        errorCode.getMessage(),
                        request.getRequestURI(),
                        validationDetails(exception),
                        correlationId(request)));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.NOTIFICATION_SAVE_FAILED;
        Throwable cause = exception.getMostSpecificCause();
        if (cause instanceof org.hibernate.exception.ConstraintViolationException cve
                && "uk_notifications_event_id".equals(cve.getConstraintName())) {
            errorCode = ErrorCode.DUPLICATE_EVENT_ID;
        }
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(
                        errorCode,
                        errorCode.getMessage(),
                        request.getRequestURI(),
                        List.of(),
                        correlationId(request)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(
                        errorCode,
                        errorCode.getMessage(),
                        request.getRequestURI(),
                        List.of(),
                        correlationId(request)));
    }

    private List<ErrorResponse.ErrorDetail> validationDetails(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            List<ErrorResponse.ErrorDetail> details = new ArrayList<>();
            methodArgumentNotValidException.getBindingResult().getFieldErrors()
                    .forEach(error -> details.add(new ErrorResponse.ErrorDetail(
                            error.getField(),
                            error.getDefaultMessage())));
            methodArgumentNotValidException.getBindingResult().getGlobalErrors()
                    .forEach(error -> details.add(new ErrorResponse.ErrorDetail(
                            error.getObjectName(),
                            error.getDefaultMessage())));
            return details;
        }
        if (exception instanceof ConstraintViolationException constraintViolationException) {
            return constraintViolationException.getConstraintViolations().stream()
                    .map(violation -> new ErrorResponse.ErrorDetail(
                            violation.getPropertyPath().toString(),
                            violation.getMessage()))
                    .toList();
        }
        if (exception instanceof HandlerMethodValidationException handlerMethodValidationException) {
            return handlerMethodValidationException.getParameterValidationResults().stream()
                    .flatMap(result -> result.getResolvableErrors().stream()
                            .map(error -> new ErrorResponse.ErrorDetail(
                                    parameterName(result),
                                    error.getDefaultMessage())))
                    .toList();
        }
        if (exception instanceof MethodArgumentTypeMismatchException mismatchException) {
            return List.of(new ErrorResponse.ErrorDetail(
                    mismatchException.getName(),
                    mismatchException.getMessage()));
        }
        if (exception instanceof MissingServletRequestParameterException missingParameterException) {
            return List.of(new ErrorResponse.ErrorDetail(
                    missingParameterException.getParameterName(),
                    missingParameterException.getMessage()));
        }
        if (exception instanceof MissingPathVariableException missingPathVariableException) {
            return List.of(new ErrorResponse.ErrorDetail(
                    missingPathVariableException.getVariableName(),
                    missingPathVariableException.getMessage()));
        }
        if (exception instanceof MissingRequestHeaderException missingRequestHeaderException) {
            return List.of(new ErrorResponse.ErrorDetail(
                    missingRequestHeaderException.getHeaderName(),
                    missingRequestHeaderException.getMessage()));
        }
        return List.of(new ErrorResponse.ErrorDetail(null, exception.getMessage()));
    }

    private String parameterName(ParameterValidationResult result) {
        String parameterName = result.getMethodParameter().getParameterName();
        if (parameterName == null || parameterName.isBlank()) {
            return result.getMethodParameter().getParameter().getName();
        }
        return parameterName;
    }

    private String correlationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            return null;
        }
        return correlationId;
    }
}
