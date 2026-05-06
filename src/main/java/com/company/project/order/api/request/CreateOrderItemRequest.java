package com.company.project.order.api.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderItemRequest(
    @NotBlank String productCode,
    @Min(1) int quantity
) {
}
