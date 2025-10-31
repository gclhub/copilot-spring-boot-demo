package com.fisglobal.demo.order.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * REST client for communicating with Customer Service.
 */
@Component
@Slf4j
public class CustomerServiceClient {

    private final RestClient restClient;
    private final String customerServiceUrl;

    public CustomerServiceClient(@Value("${customer.service.url}") String customerServiceUrl) {
        this.customerServiceUrl = customerServiceUrl;
        this.restClient = RestClient.builder()
                .baseUrl(customerServiceUrl)
                .build();
    }

    /**
     * Validate that a customer exists by ID.
     * 
     * @param customerId the customer ID to validate
     * @return Optional containing the customer if found, empty otherwise
     */
    public Optional<CustomerDTO> getCustomerById(Long customerId) {
        log.debug("Fetching customer with id: {} from {}", customerId, customerServiceUrl);
        
        try {
            CustomerDTO customer = restClient.get()
                    .uri("/api/customers/{id}", customerId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.warn("Customer not found: {}", customerId);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Customer service error for customer {}: {}", customerId, response.getStatusCode());
                    })
                    .body(CustomerDTO.class);
            
            return Optional.ofNullable(customer);
        } catch (Exception e) {
            log.error("Error communicating with customer service for customer {}: {}", customerId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Check if a customer exists by ID.
     * 
     * @param customerId the customer ID to check
     * @return true if the customer exists, false otherwise
     */
    public boolean customerExists(Long customerId) {
        return getCustomerById(customerId).isPresent();
    }
}
