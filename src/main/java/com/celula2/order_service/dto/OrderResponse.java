package com.celula2.order_service.dto;

import com.celula2.order_service.model.OrderStatus;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String productId;
    private Integer quantity;
    private String customerId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String correlationId;
}
