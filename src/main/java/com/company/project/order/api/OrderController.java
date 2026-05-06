package com.company.project.order.api;

import com.company.project.common.api.SuccessCode;
import com.company.project.common.api.ApiErrorResponse;
import com.company.project.order.api.mapper.OrderResponseMapper;
import com.company.project.order.api.request.CreateOrderRequest;
import com.company.project.order.api.response.CreateOrderResponse;
import com.company.project.order.api.response.OrderResponse;
import com.company.project.order.application.OrderApplicationService;
import com.company.project.order.application.OrderQueryService;
import com.company.project.order.application.OrderView;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;
    private final OrderQueryService orderQueryService;
    private final OrderResponseMapper orderResponseMapper;

    @GetMapping
    @Operation(summary = "Get orders", description = "Returns a paged list of orders.")
    public Page<OrderResponse> getOrders(
        @Parameter(description = "Zero-based page index", example = "0")
        @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
        @Parameter(description = "Page size", example = "20")
        @RequestParam(name = "size", defaultValue = "20") @Min(1) int size
    ) {
        Page<OrderView> orders = orderQueryService.getOrders(page, size);
        return orders.map(orderResponseMapper::toResponse);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found. Example code: ORDER_NOT_FOUND",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    public OrderResponse getOrder(@PathVariable @Min(1) Long orderId) {
        return orderResponseMapper.toResponse(orderQueryService.getOrder(orderId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create order")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order created. Example code: ORDER_CREATED"),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error. Example code: VALIDATION_ERROR",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Customer is not active. Example code: CUSTOMER_NOT_ACTIVE",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    public CreateOrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long orderId = orderApplicationService.createOrder(
            request.customerId(),
            request.items().stream()
                .map(item -> new com.company.project.order.domain.model.OrderItem(item.productCode(), item.quantity()))
                .toList(),
            request.createdBy()
        );
        return new CreateOrderResponse(orderId, SuccessCode.ORDER_CREATED.getCode());
    }
}
