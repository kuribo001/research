package com.company.project.customer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, Long> {

    boolean existsByIdAndActiveTrue(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    CustomerJpaEntity findTopByOrderByIdDesc();
}
