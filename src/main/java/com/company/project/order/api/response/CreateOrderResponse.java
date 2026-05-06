package com.company.project.order.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateOrderResponse(
    @Schema(example = "1001")
    Long id,
    @Schema(example = "ORDER_CREATED", description = "Stable success code for frontend i18n mapping")
    String code
) {
}
