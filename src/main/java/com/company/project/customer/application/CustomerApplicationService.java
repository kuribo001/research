package com.company.project.customer.application;

import com.company.project.common.api.BusinessException;
import com.company.project.common.api.ErrorCode;
import com.company.project.customer.domain.model.Customer;
import com.company.project.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerApplicationService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerView createCustomer(String code, String fullName, String email, boolean active) {
        validateDuplicateCodeForCreate(code);

        Customer saved = customerRepository.save(new Customer(
            customerRepository.nextId(),
            code,
            fullName,
            email,
            active
        ));

        return toView(saved);
    }

    @Transactional
    public CustomerView updateCustomer(Long customerId, String code, String fullName, String email, boolean active) {
        Customer existing = customerRepository.findById(customerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        validateDuplicateCodeForUpdate(code, customerId);

        Customer saved = customerRepository.save(new Customer(
            existing.getId(),
            code,
            fullName,
            email,
            active
        ));

        return toView(saved);
    }

    @Transactional
    public void deleteCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND);
        }

        customerRepository.deleteById(customerId);
    }

    private void validateDuplicateCodeForCreate(String code) {
        if (customerRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.CUSTOMER_CODE_ALREADY_EXISTS);
        }
    }

    private void validateDuplicateCodeForUpdate(String code, Long customerId) {
        if (customerRepository.existsByCodeAndIdNot(code, customerId)) {
            throw new BusinessException(ErrorCode.CUSTOMER_CODE_ALREADY_EXISTS);
        }
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
