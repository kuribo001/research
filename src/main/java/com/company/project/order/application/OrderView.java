package com.company.project.order.application;

import java.time.Instant;
import java.util.List;

public record OrderView(
    Long id,
    Long customerId,
    List<OrderItemView> items,
    String createdBy,
    Instant createdAt
) {
}
