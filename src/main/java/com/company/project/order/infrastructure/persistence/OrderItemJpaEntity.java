package com.company.project.order.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemJpaEntity {

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Column(nullable = false)
    private int quantity;

    public OrderItemJpaEntity(String productCode, int quantity) {
        this.productCode = productCode;
        this.quantity = quantity;
    }
}
