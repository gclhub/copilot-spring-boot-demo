package com.fisglobal.order.client;

import com.fisglobal.order.config.ServiceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Client Tests")
class CustomerClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ServiceConfig serviceConfig;

    private CustomerClient customerClient;

    @BeforeEach
    void setUp() {
        customerClient = new CustomerClient(restTemplate, serviceConfig);
        when(serviceConfig.getCustomerServiceUrl()).thenReturn("http://localhost:8081");
    }

    @Test
    @DisplayName("Should return customer when valid ID")
    void shouldReturnCustomerWhenValidId() {
        // Arrange
        String url = "http://localhost:8081/api/customers/1/exists";
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", true);

        when(restTemplate.getForObject(eq(url), eq(Map.class))).thenReturn(response);

        // Act
        boolean result = customerClient.validateCustomer(1L);

        // Assert
        assertThat(result).isTrue();
        verify(restTemplate, times(1)).getForObject(eq(url), eq(Map.class));
    }

    @Test
    @DisplayName("Should throw exception when customer not found")
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Arrange
        String url = "http://localhost:8081/api/customers/999/exists";
        
        when(restTemplate.getForObject(eq(url), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(NOT_FOUND));

        // Act & Assert
        assertThatThrownBy(() -> customerClient.validateCustomer(999L))
                .isInstanceOf(CustomerClient.ServiceCommunicationException.class)
                .hasMessageContaining("Unable to validate customer");

        verify(restTemplate, times(1)).getForObject(eq(url), eq(Map.class));
    }

    @Test
    @DisplayName("Should throw exception when service unavailable")
    void shouldThrowExceptionWhenServiceUnavailable() {
        // Arrange
        String url = "http://localhost:8081/api/customers/1/exists";
        
        when(restTemplate.getForObject(eq(url), eq(Map.class)))
                .thenThrow(new HttpServerErrorException(SERVICE_UNAVAILABLE));

        // Act & Assert
        assertThatThrownBy(() -> customerClient.validateCustomer(1L))
                .isInstanceOf(CustomerClient.ServiceCommunicationException.class)
                .hasMessageContaining("Unable to validate customer");

        verify(restTemplate, times(1)).getForObject(eq(url), eq(Map.class));
    }
}
