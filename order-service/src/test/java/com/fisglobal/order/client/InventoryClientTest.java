package com.fisglobal.order.client;

import com.fisglobal.order.config.ServiceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory Client Tests")
class InventoryClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ServiceConfig serviceConfig;

    private InventoryClient inventoryClient;

    @BeforeEach
    void setUp() {
        inventoryClient = new InventoryClient(restTemplate, serviceConfig);
        when(serviceConfig.getInventoryServiceUrl()).thenReturn("http://localhost:8082");
    }

    @Test
    @DisplayName("Should return product when valid ID")
    void shouldReturnProductWhenValidId() {
        // Arrange
        String url = "http://localhost:8082/api/products/1";
        Map<String, Object> productResponse = new HashMap<>();
        productResponse.put("id", 1L);
        productResponse.put("name", "Laptop");
        productResponse.put("sku", "LAPTOP-001");
        productResponse.put("price", 999.99);
        productResponse.put("stockQuantity", 50);

        when(restTemplate.getForObject(eq(url), eq(Map.class))).thenReturn(productResponse);

        // Act
        Map<String, Object> result = inventoryClient.getProduct(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("id")).isEqualTo(1L);
        assertThat(result.get("name")).isEqualTo("Laptop");
        assertThat(result.get("stockQuantity")).isEqualTo(50);
        verify(restTemplate, times(1)).getForObject(eq(url), eq(Map.class));
    }

    @Test
    @DisplayName("Should reserve stock successfully")
    void shouldReserveStockSuccessfully() {
        // Arrange
        String url = "http://localhost:8082/api/products/1/reserve?quantity=10";
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Stock reserved successfully");

        when(restTemplate.postForObject(eq(url), isNull(), eq(Map.class))).thenReturn(response);

        // Act
        boolean result = inventoryClient.reserveStock(1L, 10);

        // Assert
        assertThat(result).isTrue();
        verify(restTemplate, times(1)).postForObject(eq(url), isNull(), eq(Map.class));
    }

    @Test
    @DisplayName("Should restore stock successfully")
    void shouldRestoreStockSuccessfully() {
        // Arrange
        String url = "http://localhost:8082/api/products/1/restore?quantity=10";
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Stock restored successfully");

        when(restTemplate.postForObject(eq(url), isNull(), eq(Map.class))).thenReturn(response);

        // Act
        inventoryClient.restoreStock(1L, 10);

        // Assert
        verify(restTemplate, times(1)).postForObject(eq(url), isNull(), eq(Map.class));
    }

    @Test
    @DisplayName("Should throw exception when service unavailable")
    void shouldThrowExceptionWhenServiceUnavailable() {
        // Arrange
        String url = "http://localhost:8082/api/products/1";
        
        when(restTemplate.getForObject(eq(url), eq(Map.class)))
                .thenThrow(new HttpServerErrorException(SERVICE_UNAVAILABLE));

        // Act & Assert
        assertThatThrownBy(() -> inventoryClient.getProduct(1L))
                .isInstanceOf(InventoryClient.ServiceCommunicationException.class)
                .hasMessageContaining("Unable to fetch product");

        verify(restTemplate, times(1)).getForObject(eq(url), eq(Map.class));
    }
}
