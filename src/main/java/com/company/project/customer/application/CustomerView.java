package com.company.project.customer.application;

public record CustomerView(
    Long id,
    String code,
    String fullName,
    String email,
    boolean active
) {
}
