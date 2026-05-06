package com.company.project.customer.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCustomerRequest(
    @NotBlank String code,
    @NotBlank String fullName,
    @NotBlank @Email String email,
    @NotNull Boolean active
) {
}
