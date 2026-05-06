package com.company.project.order.api.response;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
    Long id,
    Long customerId,
    List<OrderItemResponse> items,
    String createdBy,
    Instant createdAt
) {
}
