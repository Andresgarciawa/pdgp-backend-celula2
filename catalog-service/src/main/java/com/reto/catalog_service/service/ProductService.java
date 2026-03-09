package com.reto.catalog_service.service;

import com.reto.catalog_service.dto.ProductRequest;
import com.reto.catalog_service.dto.ProductResponse;
import com.reto.catalog_service.entity.Product;
import com.reto.catalog_service.repository.ProcessedEventRepository;
import com.reto.catalog_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProcessedEventRepository processedEventRepository;


    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = findProduct(id);
        return toResponse(product);
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        return toResponse(productRepository.save(product));
    }

    public boolean checkStock(Long productId, Integer quantity) {
        validateQuantity(quantity);
        Product product = findProduct(productId);
        return product.getStock() >= quantity;
    }

    @Transactional
    public boolean applyOrderCreatedEvent(String eventId, Long productId, Integer quantity) {
        validateEventId(eventId);
        validateQuantity(quantity);

        if (!tryMarkEventAsProcessed(eventId)) {
            return false; // idempotencia: evento repetido
        }

        Product product = findProduct(productId);
        if (product.getStock() < quantity) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + productId);
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        return true;
    }

    @Transactional
    public boolean applyOrderCancelledEvent(String eventId, Long productId, Integer quantity) {
        validateEventId(eventId);
        validateQuantity(quantity);

        if (!tryMarkEventAsProcessed(eventId)) {
            return false; // idempotencia: evento repetido
        }

        Product product = findProduct(productId);
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        return true;
    }

    private boolean tryMarkEventAsProcessed(String eventId) {
            return processedEventRepository.insertIfAbsent(eventId) == 1;
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    private void validateEventId(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId es obligatorio");
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        return response;
    }
}