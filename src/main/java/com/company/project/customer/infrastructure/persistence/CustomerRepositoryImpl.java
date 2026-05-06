package com.company.project.customer.infrastructure.persistence;

import com.company.project.customer.domain.model.Customer;
import com.company.project.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerJpaRepository customerJpaRepository;

    @Override
    public Optional<Customer> findById(Long customerId) {
        return customerJpaRepository.findById(customerId).map(this::toDomain);
    }

    @Override
    public Page<Customer> findAll(Pageable pageable) {
        return customerJpaRepository.findAll(pageable).map(this::toDomain);
    }

    private Customer toDomain(CustomerJpaEntity entity) {
        return new Customer(
            entity.getId(),
            entity.getCode(),
            entity.getFullName(),
            entity.getEmail(),
            entity.isActive()
        );
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity saved = customerJpaRepository.save(toEntity(customer));
        return toDomain(saved);
    }

    @Override
    public boolean existsById(Long customerId) {
        return customerJpaRepository.existsById(customerId);
    }

    @Override
    public boolean existsActiveById(Long customerId) {
        return customerJpaRepository.existsByIdAndActiveTrue(customerId);
    }

    @Override
    public boolean existsByCode(String code) {
        return customerJpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long customerId) {
        return customerJpaRepository.existsByCodeAndIdNot(code, customerId);
    }

    @Override
    public Long nextId() {
        CustomerJpaEntity latest = customerJpaRepository.findTopByOrderByIdDesc();
        return latest == null ? 1L : latest.getId() + 1;
    }

    @Override
    public void deleteById(Long customerId) {
        customerJpaRepository.deleteById(customerId);
    }

    private CustomerJpaEntity toEntity(Customer customer) {
        return new CustomerJpaEntity(
            customer.getId(),
            customer.getCode(),
            customer.getFullName(),
            customer.getEmail(),
            customer.isActive()
        );
    }
}
