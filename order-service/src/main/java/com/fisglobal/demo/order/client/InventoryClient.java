package com.fisglobal.demo.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryClient {

    private final RestTemplate restTemplate;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public ProductDTO getProductById(Long productId) {
        log.debug("Fetching product {} from inventory service", productId);
        String url = inventoryServiceUrl + "/api/products/" + productId;
        return restTemplate.getForObject(url, ProductDTO.class);
    }

    public void reserveStock(Long productId, Integer quantity) {
        log.debug("Reserving {} units of product {} from inventory service", quantity, productId);
        String url = inventoryServiceUrl + "/api/products/" + productId + "/reserve?quantity=" + quantity;
        restTemplate.postForObject(url, null, Void.class);
    }

    public void restoreStock(Long productId, Integer quantity) {
        log.debug("Restoring {} units of product {} to inventory service", quantity, productId);
        String url = inventoryServiceUrl + "/api/products/" + productId + "/restore?quantity=" + quantity;
        restTemplate.postForObject(url, null, Void.class);
    }
}
