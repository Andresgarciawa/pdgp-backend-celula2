package com.celula2.order_service.controller;

import com.celula2.order_service.dto.CreateOrderRequest;
import com.celula2.order_service.dto.OrderResponse;
import com.celula2.order_service.model.OrderStatus;
import com.celula2.order_service.service.InsufficientStockException;
import com.celula2.order_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrder_whenRequestIsValid_returns200() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .id(UUID.randomUUID())
                .productId("prod-1")
                .quantity(2)
                .customerId("cust-1")
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.of(2026, 3, 7, 10, 30))
                .correlationId("corr-123")
                .build();

        when(orderService.createOrder(any(CreateOrderRequest.class), eq("corr-123")))
                .thenReturn(response);

        mockMvc.perform(post("/orders")
                        .header("X-Correlation-Id", "corr-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("prod-1"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.correlationId").value("corr-123"));
    }

    @Test
    void createOrder_whenStockIsInsufficient_returns409() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class), eq("corr-123")))
                .thenThrow(new InsufficientStockException("No stock available for product: prod-1"));

        mockMvc.perform(post("/orders")
                        .header("X-Correlation-Id", "corr-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("No stock available for product: prod-1"));
    }

    @Test
    void createOrder_whenRequestIsInvalid_returns400() throws Exception {
        String invalidPayload = """
                {
                  "productId": "",
                  "quantity": 0,
                  "customerId": ""
                }
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    private String validPayload() {
        return """
                {
                  "productId": "prod-1",
                  "quantity": 2,
                  "customerId": "cust-1"
                }
                """;
    }
}
