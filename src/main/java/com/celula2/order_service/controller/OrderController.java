package com.celula2.order_service.controller;

import com.celula2.order_service.dto.CreateOrderRequest;
import com.celula2.order_service.dto.OrderResponse;
import com.celula2.order_service.service.InsufficientStockException;
import com.celula2.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /orders
     * HU4: Creates an order with status CREATED → 200 OK
     * HU5: If stock is insufficient → 409 Conflict
     */
    @PostMapping
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {

        try {
            OrderResponse response = orderService.createOrder(request, correlationId);
            return ResponseEntity.ok(response);

        } catch (InsufficientStockException ex) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
