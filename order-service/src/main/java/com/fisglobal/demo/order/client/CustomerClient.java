package com.fisglobal.demo.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerClient {

    private final RestTemplate restTemplate;

    @Value("${customer.service.url}")
    private String customerServiceUrl;

    public CustomerDTO getCustomerById(Long customerId) {
        log.debug("Fetching customer {} from customer service", customerId);
        String url = customerServiceUrl + "/api/customers/" + customerId;
        return restTemplate.getForObject(url, CustomerDTO.class);
    }
}
