package com.company.project.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class OrderItem {

    private final String productCode;
    private final int quantity;

    public OrderItem(String productCode, int quantity) {
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("productCode must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.productCode = productCode;
        this.quantity = quantity;
    }
}
