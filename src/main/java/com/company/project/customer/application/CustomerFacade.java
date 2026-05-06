package com.company.project.customer.application;

public interface CustomerFacade {

    boolean existsActiveCustomer(Long customerId);

    CustomerSummary getCustomerSummary(Long customerId);
}
