package com.reto.catalog_service.service;

import com.reto.catalog_service.dto.ProductRequest;
import com.reto.catalog_service.dto.ProductResponse;
import com.reto.catalog_service.entity.Product;
import com.reto.catalog_service.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository  productRepository;

    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts(){
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(()  -> new RuntimeException("Producto no encontrado"));
        return toResponse(product);

    }

    public ProductResponse createProduct(ProductRequest request) {
        Product  product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        System.out.println("Name recibido: " +request.getName());
        System.out.println("Price recibido: " +request.getPrice());

        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(()  -> new RuntimeException("Producto no encontrado"));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        return toResponse(productRepository.save(product));
    }

    public boolean checkStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()  -> new RuntimeException("Producto no encontrado"));
        return product.getStock() >= quantity;

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