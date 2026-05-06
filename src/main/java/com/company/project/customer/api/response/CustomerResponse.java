package com.company.project.customer.api.response;

public record CustomerResponse(
    Long id,
    String code,
    String fullName,
    String email,
    boolean active
) {
}
