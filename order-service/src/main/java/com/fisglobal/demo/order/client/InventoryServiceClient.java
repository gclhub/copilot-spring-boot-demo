package com.fisglobal.demo.order.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * REST client for communicating with Inventory Service.
 */
@Component
@Slf4j
public class InventoryServiceClient {

    private final RestClient restClient;
    private final String inventoryServiceUrl;

    public InventoryServiceClient(@Value("${inventory.service.url}") String inventoryServiceUrl) {
        this.inventoryServiceUrl = inventoryServiceUrl;
        this.restClient = RestClient.builder()
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    /**
     * Get product information by ID.
     * 
     * @param productId the product ID
     * @return Optional containing the product if found, empty otherwise
     */
    public Optional<ProductDTO> getProductById(Long productId) {
        log.debug("Fetching product with id: {} from {}", productId, inventoryServiceUrl);
        
        try {
            ProductDTO product = restClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.warn("Product not found: {}", productId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Inventory service error for product {}: {}", productId, response.getStatusCode());
                    })
                    .body(ProductDTO.class);
            
            return Optional.ofNullable(product);
        } catch (Exception e) {
            log.error("Error communicating with inventory service for product {}: {}", productId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Reserve stock for a product.
     * 
     * @param productId the product ID
     * @param quantity the quantity to reserve
     * @return true if the stock was successfully reserved, false otherwise
     */
    public boolean reserveStock(Long productId, Integer quantity) {
        log.debug("Reserving {} units of product {} at {}", quantity, productId, inventoryServiceUrl);
        
        try {
            restClient.post()
                    .uri("/api/products/{id}/reserve?quantity={quantity}", productId, quantity)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.warn("Failed to reserve stock for product {}: {}", productId, response.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Inventory service error reserving stock for product {}: {}", productId, response.getStatusCode());
                    })
                    .toBodilessEntity();
            
            log.info("Successfully reserved {} units of product {}", quantity, productId);
            return true;
        } catch (Exception e) {
            log.error("Error reserving stock for product {}: {}", productId, e.getMessage());
            return false;
        }
    }

    /**
     * Restore stock for a product (compensating transaction).
     * 
     * @param productId the product ID
     * @param quantity the quantity to restore
     */
    public void restoreStock(Long productId, Integer quantity) {
        log.debug("Restoring {} units of product {} at {}", quantity, productId, inventoryServiceUrl);
        
        try {
            restClient.post()
                    .uri("/api/products/{id}/restore?quantity={quantity}", productId, quantity)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.warn("Failed to restore stock for product {}: {}", productId, response.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Inventory service error restoring stock for product {}: {}", productId, response.getStatusCode());
                    })
                    .toBodilessEntity();
            
            log.info("Successfully restored {} units of product {}", quantity, productId);
        } catch (Exception e) {
            log.error("Error restoring stock for product {}: {}", productId, e.getMessage());
        }
    }
}
