package com.company.project.order.application;

public record OrderItemView(
    String productCode,
    int quantity
) {
}
