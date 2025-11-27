package com.fisglobal.order.client;

import com.fisglobal.order.config.ServiceConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * REST client for communicating with the Inventory Service.
 * Handles product information retrieval and stock management operations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryClient {

    private final RestTemplate restTemplate;
    private final ServiceConfig serviceConfig;

    /**
     * Reserve stock for a product.
     *
     * @param productId Product ID
     * @param quantity Quantity to reserve
     * @return true if stock was successfully reserved, false if insufficient stock
     * @throws ServiceCommunicationException if unable to communicate with Inventory Service
     */
    public boolean reserveStock(Long productId, Integer quantity) {
        String url = serviceConfig.getInventoryServiceUrl() + 
                     "/api/products/" + productId + "/reserve?quantity=" + quantity;
        
        try {
            log.debug("Reserving {} units of product {} with Inventory Service at {}", 
                     quantity, productId, url);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);
            
            boolean success = response != null && Boolean.TRUE.equals(response.get("success"));
            log.debug("Stock reservation for product {} result: {}", productId, success);
            
            return success;
        } catch (RestClientException e) {
            log.error("Failed to reserve stock for product {} with Inventory Service: {}", 
                     productId, e.getMessage());
            throw new ServiceCommunicationException("Unable to reserve stock with Inventory Service", e);
        }
    }

    /**
     * Restore stock for a product (e.g., on order cancellation).
     *
     * @param productId Product ID
     * @param quantity Quantity to restore
     * @throws ServiceCommunicationException if unable to communicate with Inventory Service
     */
    public void restoreStock(Long productId, Integer quantity) {
        String url = serviceConfig.getInventoryServiceUrl() + 
                     "/api/products/" + productId + "/restore?quantity=" + quantity;
        
        try {
            log.debug("Restoring {} units of product {} with Inventory Service at {}", 
                     quantity, productId, url);
            
            restTemplate.postForObject(url, null, Map.class);
            
            log.info("Successfully restored {} units of product {}", quantity, productId);
        } catch (RestClientException e) {
            log.error("Failed to restore stock for product {} with Inventory Service: {}", 
                     productId, e.getMessage());
            throw new ServiceCommunicationException("Unable to restore stock with Inventory Service", e);
        }
    }

    /**
     * Get product information from Inventory Service.
     *
     * @param productId Product ID
     * @return Product information as a Map
     * @throws ServiceCommunicationException if unable to communicate with Inventory Service
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getProduct(Long productId) {
        String url = serviceConfig.getInventoryServiceUrl() + "/api/products/" + productId;
        
        try {
            log.debug("Fetching product {} from Inventory Service at {}", productId, url);
            
            Map<String, Object> product = restTemplate.getForObject(url, Map.class);
            
            log.debug("Retrieved product {} from Inventory Service", productId);
            return product;
        } catch (RestClientException e) {
            log.error("Failed to fetch product {} from Inventory Service: {}", 
                     productId, e.getMessage());
            throw new ServiceCommunicationException("Unable to fetch product from Inventory Service", e);
        }
    }

    /**
     * Exception thrown when unable to communicate with external services.
     */
    public static class ServiceCommunicationException extends RuntimeException {
        public ServiceCommunicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
