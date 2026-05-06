package com.company.project.order.domain.repository;

import com.company.project.order.domain.model.Order;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository {

    Optional<Order> findById(Long orderId);

    Page<Order> findAll(Pageable pageable);

    Order save(Order order);
}
