package com.company.project.order.api.response;

public record OrderItemResponse(
    String productCode,
    int quantity
) {
}
