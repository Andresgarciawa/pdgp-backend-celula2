package com.celula2.order_service.client;

import com.celula2.order_service.dto.StockCheckResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CatalogClient {

    private final RestTemplate restTemplate;

    @Value("${catalog.service.url}")
    private String catalogServiceUrl;

    public CatalogClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calls GET /catalog/check-stock?productId={id}&quantity={qty}
     * Propagates the X-Correlation-Id header (HU5)
     */
    public StockCheckResponse checkStock(String productId, int quantity, String correlationId) {
        String url = catalogServiceUrl + "/catalog/check-stock?productId=" + productId + "&quantity=" + quantity;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-Id", correlationId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<StockCheckResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, StockCheckResponse.class);

        return response.getBody();
    }
}
