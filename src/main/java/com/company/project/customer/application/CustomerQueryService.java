package com.company.project.customer.application;

import com.company.project.common.api.BusinessException;
import com.company.project.common.api.ErrorCode;
import com.company.project.customer.domain.model.Customer;
import com.company.project.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerQueryService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public CustomerView getCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        return toView(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerView> getCustomers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        return customerRepository.findAll(pageable)
            .map(this::toView);
    }

    private CustomerView toView(Customer customer) {
        return new CustomerView(
            customer.getId(),
            customer.getCode(),
            customer.getFullName(),
            customer.getEmail(),
            customer.isActive()
        );
    }
}
