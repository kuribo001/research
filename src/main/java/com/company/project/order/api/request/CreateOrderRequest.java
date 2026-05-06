package com.company.project.order.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
    @Min(1) Long customerId,
    @Valid @NotEmpty List<CreateOrderItemRequest> items,
    @NotBlank String createdBy
) {
}
