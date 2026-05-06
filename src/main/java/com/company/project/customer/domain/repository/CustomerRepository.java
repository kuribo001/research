package com.company.project.customer.domain.repository;

import com.company.project.customer.domain.model.Customer;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerRepository {

    Optional<Customer> findById(Long customerId);

    Page<Customer> findAll(Pageable pageable);

    Customer save(Customer customer);

    boolean existsById(Long customerId);

    boolean existsActiveById(Long customerId);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long customerId);

    Long nextId();

    void deleteById(Long customerId);
}
