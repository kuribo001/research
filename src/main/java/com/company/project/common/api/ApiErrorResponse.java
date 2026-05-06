package com.company.project.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
    @Schema(example = "2026-06-24T01:00:00Z")
    Instant timestamp,
    @Schema(example = "404")
    int status,
    @Schema(example = "CUSTOMER_NOT_FOUND", description = "Stable error code for frontend i18n mapping")
    String code,
    @Schema(example = "Customer not found", description = "Fallback/debug message, not final UI copy")
    String message,
    List<ApiFieldError> details,
    @Schema(example = "abc123def456")
    String traceId
) {
    public record ApiFieldError(
        @Schema(example = "email")
        String field,
        @Schema(example = "EMAIL", description = "Stable validation code for frontend i18n mapping")
        String code,
        @Schema(example = "must be a well-formed email address", description = "Fallback/debug validation message")
        String message
    ) {
    }
}
