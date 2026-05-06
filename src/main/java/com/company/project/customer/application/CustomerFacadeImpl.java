package com.company.project.customer.application;

import com.company.project.common.api.BusinessException;
import com.company.project.common.api.ErrorCode;
import com.company.project.customer.domain.model.Customer;
import com.company.project.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class CustomerFacadeImpl implements CustomerFacade {

    private final CustomerRepository customerRepository;

    @Override
    public boolean existsActiveCustomer(Long customerId) {
        return customerRepository.existsActiveById(customerId);
    }

    @Override
    public CustomerSummary getCustomerSummary(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        return new CustomerSummary(customer.getId(), customer.getCode(), customer.getFullName());
    }
}
