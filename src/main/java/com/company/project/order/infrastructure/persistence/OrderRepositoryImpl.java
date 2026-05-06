package com.company.project.order.infrastructure.persistence;

import com.company.project.order.domain.model.Order;
import com.company.project.order.domain.model.OrderItem;
import com.company.project.order.domain.repository.OrderRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderJpaRepository.findById(orderId).map(this::toDomain);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return orderJpaRepository.findAll(pageable).map(this::toDomain);
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity saved = orderJpaRepository.save(toEntity(order));
        return toDomain(saved);
    }

    private OrderJpaEntity toEntity(Order order) {
        List<OrderItemJpaEntity> items = order.getItems().stream()
            .map(item -> new OrderItemJpaEntity(item.getProductCode(), item.getQuantity()))
            .toList();

        return new OrderJpaEntity(order.getCustomerId(), order.getCreatedBy(), order.getCreatedAt(), items);
    }

    private Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
            .map(item -> new OrderItem(item.getProductCode(), item.getQuantity()))
            .toList();

        return Order.rehydrate(
            entity.getId(),
            entity.getCustomerId(),
            items,
            entity.getCreatedBy(),
            entity.getCreatedAt()
        );
    }
}
