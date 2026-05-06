package com.company.project.customer.api.mapper;

import com.company.project.customer.api.response.CustomerResponse;
import com.company.project.customer.application.CustomerView;
import org.springframework.stereotype.Component;

@Component
public class CustomerApiMapper {

    public CustomerResponse toResponse(CustomerView customerView) {
        return new CustomerResponse(
            customerView.id(),
            customerView.code(),
            customerView.fullName(),
            customerView.email(),
            customerView.active()
        );
    }
}
