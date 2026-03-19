package com.celula2.order_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrderCancelledEvent {
    private UUID eventId;
    private UUID orderId;
    private String productId;
    private Integer quantity;
    private String correlationId;
}