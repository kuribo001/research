package com.company.project.order.api.mapper;

import com.company.project.order.api.response.OrderItemResponse;
import com.company.project.order.api.response.OrderResponse;
import com.company.project.order.application.OrderView;
import org.springframework.stereotype.Component;

@Component
public class OrderResponseMapper {

    public OrderResponse toResponse(OrderView orderView) {
        return new OrderResponse(
            orderView.id(),
            orderView.customerId(),
            orderView.items().stream()
                .map(item -> new OrderItemResponse(item.productCode(), item.quantity()))
                .toList(),
            orderView.createdBy(),
            orderView.createdAt()
        );
    }
}
