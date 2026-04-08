package com.fisglobal.demo.order.service;

import com.fisglobal.demo.order.client.CustomerDTO;
import com.fisglobal.demo.order.client.CustomerServiceClient;
import com.fisglobal.demo.order.client.InventoryServiceClient;
import com.fisglobal.demo.order.client.ProductDTO;
import com.fisglobal.demo.order.dto.CreateOrderRequest;
import com.fisglobal.demo.order.dto.OrderItemRequest;
import com.fisglobal.demo.order.model.Order;
import com.fisglobal.demo.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService with mocked inter-service communication.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerServiceClient customerServiceClient;

    @Mock
    private InventoryServiceClient inventoryServiceClient;

    @InjectMocks
    private OrderService orderService;

    private CustomerDTO testCustomer;
    private ProductDTO testProduct;
    private CreateOrderRequest testOrderRequest;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testCustomer = new CustomerDTO();
        testCustomer.setId(1L);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@example.com");

        testProduct = new ProductDTO();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setSku("LAPTOP-001");
        testProduct.setPrice(new BigDecimal("1299.99"));
        testProduct.setStockQuantity(50);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        testOrderRequest = new CreateOrderRequest();
        testOrderRequest.setCustomerId(1L);
        testOrderRequest.setItems(Arrays.asList(itemRequest));
        testOrderRequest.setShippingAddress("123 Main St");
        testOrderRequest.setShippingCity("New York");
        testOrderRequest.setShippingState("NY");
        testOrderRequest.setShippingZip("10001");
        testOrderRequest.setShippingCountry("USA");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomerId(1L);
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Given
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertThat(result).hasSize(1);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When
        Optional<Order> result = orderService.getOrderById(1L);

        // Then
        assertThat(result).isPresent();
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void createOrder_WhenCustomerAndProductExist_ShouldCreateOrder() {
        // Given
        when(customerServiceClient.getCustomerById(1L)).thenReturn(Optional.of(testCustomer));
        when(inventoryServiceClient.getProductById(1L)).thenReturn(Optional.of(testProduct));
        when(inventoryServiceClient.reserveStock(1L, 2)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.createOrder(testOrderRequest);

        // Then
        assertThat(result).isNotNull();
        verify(customerServiceClient, times(1)).getCustomerById(1L);
        verify(inventoryServiceClient, times(1)).getProductById(1L);
        verify(inventoryServiceClient, times(1)).reserveStock(1L, 2);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_WhenCustomerNotFound_ShouldThrowException() {
        // Given
        when(customerServiceClient.getCustomerById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(testOrderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer not found");

        verify(customerServiceClient, times(1)).getCustomerById(1L);
        verify(inventoryServiceClient, never()).getProductById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WhenProductNotFound_ShouldThrowException() {
        // Given
        when(customerServiceClient.getCustomerById(1L)).thenReturn(Optional.of(testCustomer));
        when(inventoryServiceClient.getProductById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(testOrderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");

        verify(customerServiceClient, times(1)).getCustomerById(1L);
        verify(inventoryServiceClient, times(1)).getProductById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WhenStockReservationFails_ShouldThrowExceptionAndRollback() {
        // Given
        when(customerServiceClient.getCustomerById(1L)).thenReturn(Optional.of(testCustomer));
        when(inventoryServiceClient.getProductById(1L)).thenReturn(Optional.of(testProduct));
        when(inventoryServiceClient.reserveStock(1L, 2)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(testOrderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");

        verify(inventoryServiceClient, times(1)).reserveStock(1L, 2);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_ToCancelled_ShouldRestoreInventory() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        orderService.updateOrderStatus(1L, Order.OrderStatus.CANCELLED);

        // Then
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void deleteOrder_WhenOrderNotCancelled_ShouldRestoreInventory() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When
        orderService.deleteOrder(1L);

        // Then
        verify(orderRepository, times(1)).deleteById(1L);
    }
}
