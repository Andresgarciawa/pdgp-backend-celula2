package com.reto.catalog_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "orders.exchange";
    public static final String QUEUE_CREATED = "catalog.order.created";
    public static final String QUEUE_CANCELLED = "catalog.order.cancelled";
    public static final String ROUTING_KEY_CREATED = "order.created";
    public static final String ROUTING_KEY_CANCELLED = "order.cancelled";

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue queueCreated() {
        return new Queue(QUEUE_CREATED, true);
    }

    @Bean
    public Queue queueCancelled() {
        return new Queue(QUEUE_CANCELLED, true);
    }

    @Bean
    public Binding bindingCreated() {
        return BindingBuilder.bind(queueCreated())
                .to(ordersExchange())
                .with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding bindingCancelled(){
        return BindingBuilder.bind(queueCancelled())
                .to(ordersExchange())
                .with(ROUTING_KEY_CANCELLED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
