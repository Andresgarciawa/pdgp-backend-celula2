package com.reto.api_gateway.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class DownstreamHealthConfig {

    @Bean
    public WebClient gatewayWebClient() {
        return WebClient.builder().build();
    }

    @Bean(name = "authService")
    public ReactiveHealthIndicator authServiceHealthIndicator(WebClient gatewayWebClient) {
        return checkService(gatewayWebClient, "http://localhost:8081/actuator/health", "auth-service");
    }

    @Bean(name = "catalogService")
    public ReactiveHealthIndicator catalogServiceHealthIndicator(WebClient gatewayWebClient) {
        return checkService(gatewayWebClient, "http://localhost:8083/actuator/health", "catalog-service");
    }

    @Bean(name = "ordersService")
    public ReactiveHealthIndicator ordersServiceHealthIndicator(WebClient gatewayWebClient) {
        return checkService(gatewayWebClient, "http://localhost:8082/actuator/health", "orders-service");
    }

    private ReactiveHealthIndicator checkService(WebClient client, String url, String serviceName) {
        return () -> client.get()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .map(response -> Health.up()
                        .withDetail("service", serviceName)
                        .withDetail("statusCode", response.getStatusCode().value())
                        .build())
                .onErrorResume(ex -> Mono.just(Health.down(ex)
                        .withDetail("service", serviceName)
                        .build()));
    }
}
