package com.celula2.order_service.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockCheckResponse {
    private String productId;
    private Integer availableQuantity;
    private boolean available;
}
