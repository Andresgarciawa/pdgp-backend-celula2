package com.reto.catalog_service.messaging;

import com.reto.catalog_service.config.RabbitMQConfig;
import com.reto.catalog_service.service.ProductService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderEventConsumer {

    private final ProductService productService;

    public OrderEventConsumer(ProductService productService) {
        this.productService = productService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CREATED)
    public void handleOrderCreated(Map<String, Object> event) {
        String eventId = (String) event.get("eventId") != null
                ? (String) event.get("eventId")
                : (String) event.get("id");

        Long productId = Long.valueOf(event.get("productId").toString());
        Integer quantity = Integer.valueOf(event.get("quantity").toString());

        boolean processed = productService.applyOrderCreatedEvent(eventId, productId, quantity);
        if (!processed) {
            System.out.println("Evento duplicado ignorado: " + eventId);
            return;
        }

        System.out.println("Stock descontado para producto: " + productId);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CANCELLED)
    public void handleOrderCancelled(Map<String, Object> event) {
        String eventId = (String) event.get("eventId");
        Long productId = Long.valueOf(event.get("productId").toString());
        Integer quantity = Integer.valueOf(event.get("quantity").toString());

        boolean processed = productService.applyOrderCancelledEvent(eventId, productId, quantity);
        if (!processed) {
            System.out.println("Evento duplicado ignorado: " + eventId);
            return;
        }

        System.out.println("Stock repuesto para producto: " + productId);
    }
}