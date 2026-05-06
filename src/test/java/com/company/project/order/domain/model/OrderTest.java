package com.company.project.order.domain.model;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    void shouldCreateOrderWhenInputIsValid() {
        assertDoesNotThrow(() -> Order.create(
            1L,
            List.of(new OrderItem("SKU-001", 2)),
            "system"
        ));
    }

    @Test
    void shouldThrowWhenItemsAreEmpty() {
        assertThrows(IllegalArgumentException.class, () -> Order.create(1L, List.of(), "system"));
    }
}
