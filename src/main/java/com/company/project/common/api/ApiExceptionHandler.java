package com.company.project.common.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        List<ApiErrorResponse.ApiFieldError> details = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::toFieldError)
            .toList();

        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus()).body(new ApiErrorResponse(
            Instant.now(),
            ErrorCode.VALIDATION_ERROR.getStatus().value(),
            ErrorCode.VALIDATION_ERROR.getCode(),
            ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
            details,
            resolveTraceId(request)
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
        ConstraintViolationException exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus()).body(new ApiErrorResponse(
            Instant.now(),
            ErrorCode.VALIDATION_ERROR.getStatus().value(),
            ErrorCode.VALIDATION_ERROR.getCode(),
            ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
            List.of(),
            resolveTraceId(request)
        ));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
        BusinessException exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(exception.getStatus()).body(new ApiErrorResponse(
            Instant.now(),
            exception.getStatus().value(),
            exception.getCode(),
            exception.getMessage(),
            List.of(),
            resolveTraceId(request)
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnhandledException(
        Exception exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(new ApiErrorResponse(
            Instant.now(),
            ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value(),
            ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(),
            List.of(),
            resolveTraceId(request)
        ));
    }

    private ApiErrorResponse.ApiFieldError toFieldError(FieldError error) {
        return new ApiErrorResponse.ApiFieldError(
            error.getField(),
            resolveValidationCode(error),
            error.getDefaultMessage()
        );
    }

    private String resolveValidationCode(FieldError error) {
        return Optional.ofNullable(error.getCode())
            .map(code -> code.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT))
            .orElse("INVALID");
    }

    private String resolveTraceId(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Trace-Id"))
            .filter(value -> !value.isBlank())
            .orElseGet(() -> UUID.randomUUID().toString().replace("-", "").substring(0, 12));
    }
}
