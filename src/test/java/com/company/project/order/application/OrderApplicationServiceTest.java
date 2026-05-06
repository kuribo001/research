package com.company.project.order.application;

import com.company.project.common.api.BusinessException;
import com.company.project.customer.application.CustomerFacade;
import com.company.project.customer.application.CustomerSummary;
import com.company.project.order.domain.model.Order;
import com.company.project.order.domain.model.OrderItem;
import com.company.project.order.domain.repository.OrderRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private CustomerFacade customerFacade;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderApplicationService orderApplicationService;

    @Test
    void shouldCreateOrderWhenCustomerIsActive() {
        when(customerFacade.existsActiveCustomer(1L)).thenReturn(true);
        when(customerFacade.getCustomerSummary(1L)).thenReturn(new CustomerSummary(1L, "CUST-001", "Nguyen Van An"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.assignId(1001L);
            return order;
        });

        Long orderId = orderApplicationService.createOrder(
            1L,
            List.of(new OrderItem("SKU-001", 2)),
            "system"
        );

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        assertEquals(1001L, orderId);
        assertEquals(1L, captor.getValue().getCustomerId());
        assertEquals("system", captor.getValue().getCreatedBy());
        assertEquals(1, captor.getValue().getItems().size());
    }

    @Test
    void shouldThrowWhenCustomerIsNotActive() {
        when(customerFacade.existsActiveCustomer(1L)).thenReturn(false);

        assertThrows(BusinessException.class, () -> orderApplicationService.createOrder(
            1L,
            List.of(new OrderItem("SKU-001", 2)),
            "system"
        ));
    }
}
