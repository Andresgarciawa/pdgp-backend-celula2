package com.reto.catalog_service.messaging;

import com.reto.catalog_service.config.RabbitMQConfig;
import com.reto.catalog_service.entity.ProcessedEvent;
import com.reto.catalog_service.entity.Product;
import com.reto.catalog_service.repository.ProcessedEventRepository;
import com.reto.catalog_service.repository.ProductRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderEventConsumer {

    private final ProductRepository productRepository;
    private final ProcessedEventRepository processedEventRepository;

    public OrderEventConsumer(ProductRepository productRepository,
                              ProcessedEventRepository processedEventRepository) {
        this.productRepository = productRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CREATED)
    public void handleOrderCreated(Map<String, Object> event) {
        String eventId = (String) event.get("eventId");

        if (processedEventRepository.existsById(eventId)) {
            System.out.println("Evento ya procesado: " + eventId);
            return;
        }

        Long productId = Long.valueOf(event.get("productId").toString());
        Integer quantity = Integer.valueOf(event.get("quantity").toString());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        processedEventRepository.save(new ProcessedEvent(eventId));

        System.out.println("Stock descontado para producto: " + productId);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CANCELLED)
    public void handleOrderCancelled(Map<String, Object> event) {
        String eventId = (String) event.get("eventId");

        if (processedEventRepository.existsById(eventId)) {
            System.out.println("Evento ya procesado: " + eventId);
            return;
        }

        Long productId = Long.valueOf(event.get("productId").toString());
        Integer quantity = Integer.valueOf(event.get("quantity").toString());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        processedEventRepository.save(new ProcessedEvent(eventId));

        System.out.println("Stock repuesto para producto: " + productId);
    }
}