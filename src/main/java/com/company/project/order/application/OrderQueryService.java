package com.company.project.order.application;

import com.company.project.common.api.BusinessException;
import com.company.project.common.api.ErrorCode;
import com.company.project.order.domain.model.Order;
import com.company.project.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public OrderView getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        return toView(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderView> getOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        return orderRepository.findAll(pageable)
            .map(this::toView);
    }

    private OrderView toView(Order order) {
        return new OrderView(
            order.getId(),
            order.getCustomerId(),
            order.getItems().stream()
                .map(item -> new OrderItemView(item.getProductCode(), item.getQuantity()))
                .toList(),
            order.getCreatedBy(),
            order.getCreatedAt()
        );
    }
}
