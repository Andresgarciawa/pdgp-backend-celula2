package com.celula2.order_service.service;

import com.celula2.order_service.client.CatalogClient;
import com.celula2.order_service.config.RabbitMQConfig;
import com.celula2.order_service.dto.CreateOrderRequest;
import com.celula2.order_service.dto.OrderResponse;
import com.celula2.order_service.dto.StockCheckResponse;
import com.celula2.order_service.model.Order;
import com.celula2.order_service.model.OrderStatus;
import com.celula2.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;
    private final RabbitTemplate rabbitTemplate;

    /**
     * HU4 + HU5: Validates stock then creates the order.
     *
     * @param request       order data
     * @param correlationId X-Correlation-Id header (propagated to catalog)
     * @return saved order as DTO
     * @throws InsufficientStockException if catalog says stock is unavailable
     */
    public OrderResponse createOrder(CreateOrderRequest request, String correlationId) {

        // Generate correlationId if not provided
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        log.info("[{}] Checking stock for product {} qty {}",
                correlationId, request.getProductId(), request.getQuantity());

        // HU5 – Validate stock via REST
        StockCheckResponse stock = catalogClient.checkStock(
                request.getProductId(), request.getQuantity(), correlationId);

        if (stock == null || !stock.isAvailable()) {
            log.warn("[{}] Insufficient stock for product {}", correlationId, request.getProductId());
            throw new InsufficientStockException(
                    "No stock available for product: " + request.getProductId());
        }

        // HU4 – Persist order with status CREATED
        Order order = Order.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .customerId(request.getCustomerId())
                .status(OrderStatus.CREATED)
                .correlationId(correlationId)
                .build();

        Order saved = orderRepository.save(order);
        log.info("[{}] Order created with id {}", correlationId, saved.getId());

        // Publish event to RabbitMQ
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                toResponse(saved));

        return toResponse(saved);
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .correlationId(order.getCorrelationId())
                .build();
    }
}
