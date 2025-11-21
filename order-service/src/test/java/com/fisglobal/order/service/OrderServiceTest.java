package com.fisglobal.order.service;

import com.fisglobal.order.client.CustomerClient;
import com.fisglobal.order.client.InventoryClient;
import com.fisglobal.order.dto.CreateOrderRequest;
import com.fisglobal.order.dto.OrderItemRequest;
import com.fisglobal.order.model.Order;
import com.fisglobal.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService.
 * Demonstrates mocking external service calls to Customer and Inventory services.
 * Tests follow AAA (Arrange-Act-Assert) pattern and use JUnit 5 best practices.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest testOrderRequest;
    private Map<String, Object> testProduct;

    @BeforeEach
    void setUp() {
        // Setup test order request
        OrderItemRequest item = new OrderItemRequest(1L, 2);
        testOrderRequest = new CreateOrderRequest();
        testOrderRequest.setCustomerId(1L);
        testOrderRequest.setItems(Collections.singletonList(item));
        testOrderRequest.setShippingAddress("123 Main St");
        testOrderRequest.setShippingCity("New York");
        testOrderRequest.setShippingState("NY");
        testOrderRequest.setShippingZip("10001");
        testOrderRequest.setShippingCountry("USA");

        // Setup test product response from Inventory Service
        testProduct = new HashMap<>();
        testProduct.put("id", 1);
        testProduct.put("name", "Test Product");
        testProduct.put("sku", "TEST-001");
        testProduct.put("price", 99.99);
        testProduct.put("stockQuantity", 100);
    }

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order when valid data and services are available")
        void shouldCreateOrderWhenValidDataAndServicesAreAvailable() {
            // Arrange
            when(customerClient.validateCustomer(1L)).thenReturn(true);
            when(inventoryClient.getProduct(1L)).thenReturn(testProduct);
            when(inventoryClient.reserveStock(1L, 2)).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });

            // Act
            Order result = orderService.createOrder(testOrderRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getCustomerId());
            assertEquals(Order.OrderStatus.CONFIRMED, result.getStatus());
            assertEquals(1, result.getItems().size());
            
            verify(customerClient, times(1)).validateCustomer(1L);
            verify(inventoryClient, times(1)).getProduct(1L);
            verify(inventoryClient, times(1)).reserveStock(1L, 2);
            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when customer does not exist")
        void shouldThrowExceptionWhenCustomerDoesNotExist() {
            // Arrange
            when(customerClient.validateCustomer(1L)).thenReturn(false);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> orderService.createOrder(testOrderRequest)
            );
            assertTrue(exception.getMessage().contains("Customer not found"));
            
            verify(customerClient, times(1)).validateCustomer(1L);
            verify(inventoryClient, never()).getProduct(anyLong());
            verify(inventoryClient, never()).reserveStock(anyLong(), anyInt());
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when product does not exist")
        void shouldThrowExceptionWhenProductDoesNotExist() {
            // Arrange
            when(customerClient.validateCustomer(1L)).thenReturn(true);
            when(inventoryClient.getProduct(1L)).thenReturn(null);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> orderService.createOrder(testOrderRequest)
            );
            assertTrue(exception.getMessage().contains("Product not found"));
            
            verify(customerClient, times(1)).validateCustomer(1L);
            verify(inventoryClient, times(1)).getProduct(1L);
            verify(inventoryClient, never()).reserveStock(anyLong(), anyInt());
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when product is out of stock")
        void shouldThrowExceptionWhenProductIsOutOfStock() {
            // Arrange
            testProduct.put("stockQuantity", 0);
            when(customerClient.validateCustomer(1L)).thenReturn(true);
            when(inventoryClient.getProduct(1L)).thenReturn(testProduct);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> orderService.createOrder(testOrderRequest)
            );
            assertTrue(exception.getMessage().contains("out of stock"));
            
            verify(customerClient, times(1)).validateCustomer(1L);
            verify(inventoryClient, times(1)).getProduct(1L);
            verify(inventoryClient, never()).reserveStock(anyLong(), anyInt());
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception and restore stock when reservation fails")
        void shouldThrowExceptionAndRestoreStockWhenReservationFails() {
            // Arrange
            when(customerClient.validateCustomer(1L)).thenReturn(true);
            when(inventoryClient.getProduct(1L)).thenReturn(testProduct);
            when(inventoryClient.reserveStock(1L, 2)).thenReturn(false);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> orderService.createOrder(testOrderRequest)
            );
            assertTrue(exception.getMessage().contains("Insufficient stock"));
            
            verify(customerClient, times(1)).validateCustomer(1L);
            verify(inventoryClient, times(1)).getProduct(1L);
            verify(inventoryClient, times(1)).reserveStock(1L, 2);
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Update Order Status Tests")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should restore inventory when order is cancelled")
        void shouldRestoreInventoryWhenOrderIsCancelled() {
            // Arrange
            Order order = new Order();
            order.setId(1L);
            order.setStatus(Order.OrderStatus.CONFIRMED);
            order.setCustomerId(1L);
            
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            // Act
            Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.CANCELLED);

            // Assert
            assertNotNull(result);
            assertEquals(Order.OrderStatus.CANCELLED, result.getStatus());
            
            verify(orderRepository, times(1)).findById(1L);
            verify(orderRepository, times(1)).save(order);
        }

        @Test
        @DisplayName("Should not restore inventory when order already cancelled")
        void shouldNotRestoreInventoryWhenOrderAlreadyCancelled() {
            // Arrange
            Order order = new Order();
            order.setId(1L);
            order.setStatus(Order.OrderStatus.CANCELLED);
            order.setCustomerId(1L);
            
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            // Act
            Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.CANCELLED);

            // Assert
            assertNotNull(result);
            assertEquals(Order.OrderStatus.CANCELLED, result.getStatus());
            
            verify(orderRepository, times(1)).findById(1L);
            verify(orderRepository, times(1)).save(order);
            verify(inventoryClient, never()).restoreStock(anyLong(), anyInt());
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent order")
        void shouldThrowExceptionWhenUpdatingNonExistentOrder() {
            // Arrange
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> orderService.updateOrderStatus(999L, Order.OrderStatus.CANCELLED)
            );
            
            verify(orderRepository, times(1)).findById(999L);
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Get Order Tests")
    class GetOrderTests {

        @Test
        @DisplayName("Should return order when valid ID is provided")
        void shouldReturnOrderWhenValidIdIsProvided() {
            // Arrange
            Order order = new Order();
            order.setId(1L);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            // Act
            Optional<Order> result = orderService.getOrderById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
            verify(orderRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should return empty when order ID does not exist")
        void shouldReturnEmptyWhenOrderIdDoesNotExist() {
            // Arrange
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Order> result = orderService.getOrderById(999L);

            // Assert
            assertFalse(result.isPresent());
            verify(orderRepository, times(1)).findById(999L);
        }
    }
}
