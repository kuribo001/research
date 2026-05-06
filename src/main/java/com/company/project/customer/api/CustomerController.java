package com.company.project.customer.api;

import com.company.project.customer.api.mapper.CustomerApiMapper;
import com.company.project.customer.api.request.CreateCustomerRequest;
import com.company.project.customer.api.request.UpdateCustomerRequest;
import com.company.project.customer.api.response.CustomerResponse;
import com.company.project.customer.application.CustomerApplicationService;
import com.company.project.customer.application.CustomerQueryService;
import com.company.project.customer.application.CustomerView;
import com.company.project.common.api.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerApplicationService customerApplicationService;
    private final CustomerQueryService customerQueryService;
    private final CustomerApiMapper customerApiMapper;

    @GetMapping
    @Operation(summary = "Get customers", description = "Returns a paged list of customers.")
    public Page<CustomerResponse> getCustomers(
        @Parameter(description = "Zero-based page index", example = "0")
        @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(name = "size", defaultValue = "20") @Min(1) int size
    ) {
        Page<CustomerView> customers = customerQueryService.getCustomers(page, size);
        return customers.map(customerApiMapper::toResponse);
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer found"),
        @ApiResponse(
            responseCode = "404",
            description = "Customer not found. Example code: CUSTOMER_NOT_FOUND",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    public CustomerResponse getCustomer(@PathVariable @Min(1) Long customerId) {
        return customerApiMapper.toResponse(customerQueryService.getCustomer(customerId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create customer")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Customer created"),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error. Example code: VALIDATION_ERROR",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Customer code already exists. Example code: CUSTOMER_CODE_ALREADY_EXISTS",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    public CustomerResponse createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return customerApiMapper.toResponse(
            customerApplicationService.createCustomer(
                request.code(),
                request.fullName(),
                request.email(),
                request.active()
            )
        );
    }

    @PutMapping("/{customerId}")
    @Operation(summary = "Update customer")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer updated"),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error. Example code: VALIDATION_ERROR",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Customer not found. Example code: CUSTOMER_NOT_FOUND",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Customer code already exists. Example code: CUSTOMER_CODE_ALREADY_EXISTS",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    public CustomerResponse updateCustomer(
        @PathVariable @Min(1) Long customerId,
        @Valid @RequestBody UpdateCustomerRequest request
    ) {
        return customerApiMapper.toResponse(
            customerApplicationService.updateCustomer(
                customerId,
                request.code(),
                request.fullName(),
                request.email(),
                request.active()
            )
        );
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete customer")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Customer deleted"),
        @ApiResponse(
            responseCode = "404",
            description = "Customer not found. Example code: CUSTOMER_NOT_FOUND",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    public void deleteCustomer(@PathVariable @Min(1) Long customerId) {
        customerApplicationService.deleteCustomer(customerId);
    }
}
