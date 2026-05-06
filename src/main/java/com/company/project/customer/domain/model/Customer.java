package com.company.project.customer.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Customer {

    private final Long id;
    private final String code;
    private final String fullName;
    private final String email;
    private final boolean active;

}
