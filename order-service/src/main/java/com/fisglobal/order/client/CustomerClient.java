package com.fisglobal.order.client;

import com.fisglobal.order.config.ServiceConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * REST client for communicating with the Customer Service.
 * Handles customer validation for order processing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerClient {

    private final RestTemplate restTemplate;
    private final ServiceConfig serviceConfig;

    /**
     * Validate if a customer exists.
     *
     * @param customerId Customer ID to validate
     * @return true if customer exists, false otherwise
     * @throws ServiceCommunicationException if unable to communicate with Customer Service
     */
    public boolean validateCustomer(Long customerId) {
        String url = serviceConfig.getCustomerServiceUrl() + "/api/customers/" + customerId + "/exists";
        
        try {
            log.debug("Validating customer {} with Customer Service at {}", customerId, url);
            
            @SuppressWarnings("unchecked")
            Map<String, Boolean> response = restTemplate.getForObject(url, Map.class);
            
            boolean exists = response != null && Boolean.TRUE.equals(response.get("exists"));
            log.debug("Customer {} validation result: {}", customerId, exists);
            
            return exists;
        } catch (RestClientException e) {
            log.error("Failed to validate customer {} with Customer Service: {}", customerId, e.getMessage());
            throw new ServiceCommunicationException("Unable to validate customer with Customer Service", e);
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
