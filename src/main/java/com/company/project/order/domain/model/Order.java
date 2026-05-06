package com.company.project.order.domain.model;

import java.time.Instant;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Order {

    private Long id;
    private final Long customerId;
    private final List<OrderItem> items;
    private final String createdBy;
    private final Instant createdAt;

    private Order(Long id, Long customerId, List<OrderItem> items, String createdBy, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.items = List.copyOf(items);
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public static Order create(Long customerId, List<OrderItem> items, String createdBy) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("customerId must be positive");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        if (createdBy == null || createdBy.isBlank()) {
            throw new IllegalArgumentException("createdBy must not be blank");
        }

        return new Order(null, customerId, items, createdBy, Instant.now());
    }

    public static Order rehydrate(Long id, Long customerId, List<OrderItem> items, String createdBy, Instant createdAt) {
        return new Order(id, customerId, items, createdBy, createdAt);
    }

    public void assignId(Long id) {
        this.id = id;
    }
}
