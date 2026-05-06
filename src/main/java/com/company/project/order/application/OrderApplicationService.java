package com.company.project.order.application;

import com.company.project.common.api.BusinessException;
import com.company.project.common.api.ErrorCode;
import com.company.project.customer.application.CustomerFacade;
import com.company.project.customer.application.CustomerSummary;
import com.company.project.order.domain.model.Order;
import com.company.project.order.domain.model.OrderItem;
import com.company.project.order.domain.repository.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final CustomerFacade customerFacade;
    private final OrderRepository orderRepository;

    @Transactional
    public Long createOrder(Long customerId, List<OrderItem> items, String createdBy) {
        if (!customerFacade.existsActiveCustomer(customerId)) {
            throw new BusinessException(ErrorCode.CUSTOMER_NOT_ACTIVE);
        }

        CustomerSummary customer = customerFacade.getCustomerSummary(customerId);
        Order order = Order.create(customer.id(), items, createdBy);
        return orderRepository.save(order).getId();
    }
}
