package com.celula2.order_service.service;

import com.celula2.order_service.client.CatalogClient;
import com.celula2.order_service.config.RabbitMQConfig;
import com.celula2.order_service.dto.CreateOrderRequest;
import com.celula2.order_service.dto.OrderResponse;
import com.celula2.order_service.dto.StockCheckResponse;
import com.celula2.order_service.model.Order;
import com.celula2.order_service.model.OrderStatus;
import com.celula2.order_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_whenStockAvailable_persistsAndPublishesOrder() {
        CreateOrderRequest request = buildRequest();
        when(catalogClient.checkStock("prod-1", 2, "corr-123"))
                .thenReturn(StockCheckResponse.builder().available(true).availableQuantity(10).build());

        UUID orderId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 7, 10, 0);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(orderId);
            order.setCreatedAt(createdAt);
            return order;
        });

        OrderResponse response = orderService.createOrder(request, "corr-123");

        assertEquals(orderId, response.getId());
        assertEquals("prod-1", response.getProductId());
        assertEquals(2, response.getQuantity());
        assertEquals("cust-1", response.getCustomerId());
        assertEquals(OrderStatus.CREATED, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals("corr-123", response.getCorrelationId());

        ArgumentCaptor<Order> savedOrderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(savedOrderCaptor.capture());
        Order savedOrder = savedOrderCaptor.getValue();
        assertEquals(OrderStatus.CREATED, savedOrder.getStatus());
        assertEquals("corr-123", savedOrder.getCorrelationId());

        ArgumentCaptor<OrderResponse> eventCaptor = ArgumentCaptor.forClass(OrderResponse.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ORDER_EXCHANGE),
                eq(RabbitMQConfig.ORDER_ROUTING_KEY),
                eventCaptor.capture());
        assertEquals(orderId, eventCaptor.getValue().getId());
    }

    @Test
    void createOrder_whenCorrelationIdMissing_generatesOneAndPropagatesIt() {
        CreateOrderRequest request = buildRequest();
        when(catalogClient.checkStock(eq("prod-1"), eq(2), anyString()))
                .thenReturn(StockCheckResponse.builder().available(true).availableQuantity(10).build());

        UUID orderId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 7, 10, 15);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(orderId);
            order.setCreatedAt(createdAt);
            return order;
        });

        OrderResponse response = orderService.createOrder(request, "   ");

        ArgumentCaptor<String> correlationCaptor = ArgumentCaptor.forClass(String.class);
        verify(catalogClient).checkStock(eq("prod-1"), eq(2), correlationCaptor.capture());
        String generatedCorrelationId = correlationCaptor.getValue();

        assertNotNull(generatedCorrelationId);
        assertFalse(generatedCorrelationId.isBlank());
        assertEquals(generatedCorrelationId, response.getCorrelationId());

        ArgumentCaptor<Order> savedOrderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(savedOrderCaptor.capture());
        assertEquals(generatedCorrelationId, savedOrderCaptor.getValue().getCorrelationId());
    }

    @Test
    void createOrder_whenStockUnavailable_throwsAndDoesNotPersistOrPublish() {
        CreateOrderRequest request = buildRequest();
        when(catalogClient.checkStock("prod-1", 2, "corr-123"))
                .thenReturn(StockCheckResponse.builder().available(false).availableQuantity(0).build());

        assertThrows(InsufficientStockException.class,
                () -> orderService.createOrder(request, "corr-123"));

        verify(orderRepository, never()).save(any(Order.class));
        verify(rabbitTemplate, never()).convertAndSend(
                eq(RabbitMQConfig.ORDER_EXCHANGE),
                eq(RabbitMQConfig.ORDER_ROUTING_KEY),
                any(OrderResponse.class));
    }

    private CreateOrderRequest buildRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId("prod-1");
        request.setQuantity(2);
        request.setCustomerId("cust-1");
        return request;
    }
}
