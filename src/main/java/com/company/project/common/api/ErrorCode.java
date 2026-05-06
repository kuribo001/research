package com.company.project.common.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Unexpected system error", HttpStatus.INTERNAL_SERVER_ERROR),
    CUSTOMER_NOT_FOUND("CUSTOMER_NOT_FOUND", "Customer not found", HttpStatus.NOT_FOUND),
    CUSTOMER_NOT_ACTIVE("CUSTOMER_NOT_ACTIVE", "Customer is not active", HttpStatus.UNPROCESSABLE_ENTITY),
    CUSTOMER_CODE_ALREADY_EXISTS("CUSTOMER_CODE_ALREADY_EXISTS", "Customer code already exists", HttpStatus.CONFLICT),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;
}
