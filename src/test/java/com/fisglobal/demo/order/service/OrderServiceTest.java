package com.fisglobal.demo.order.service;

import com.fisglobal.demo.customer.model.Customer;
import com.fisglobal.demo.customer.service.CustomerService;
import com.fisglobal.demo.inventory.model.Product;
import com.fisglobal.demo.inventory.service.ProductService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private Customer testCustomer;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setEmail("test@example.com");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setSku("TEST-001");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(100);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomerId(1L);
        testOrder.setOrderNumber("ORD-12345");
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Arrange
        Order order2 = new Order();
        order2.setId(2L);
        List<Order> orders = Arrays.asList(testOrder, order2);
        when(orderRepository.findAll()).thenReturn(orders);

        // Act
        List<Order> result = orderService.getAllOrders();

        // Assert
        assertEquals(2, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        Optional<Order> result = orderService.getOrderById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("ORD-12345", result.get().getOrderNumber());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrderById_WhenOrderDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = orderService.getOrderById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(orderRepository, times(1)).findById(999L);
    }

    @Test
    void getOrderByOrderNumber_WhenOrderExists_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findByOrderNumber("ORD-12345")).thenReturn(Optional.of(testOrder));

        // Act
        Optional<Order> result = orderService.getOrderByOrderNumber("ORD-12345");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("ORD-12345", result.get().getOrderNumber());
        verify(orderRepository, times(1)).findByOrderNumber("ORD-12345");
    }

    @Test
    void getOrdersByCustomerId_ShouldReturnCustomerOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByCustomerId(1L)).thenReturn(orders);

        // Act
        List<Order> result = orderService.getOrdersByCustomerId(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCustomerId());
        verify(orderRepository, times(1)).findByCustomerId(1L);
    }

    @Test
    void getOrdersByStatus_ShouldReturnOrdersWithStatus() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(Order.OrderStatus.CONFIRMED)).thenReturn(orders);

        // Act
        List<Order> result = orderService.getOrdersByStatus(Order.OrderStatus.CONFIRMED);

        // Assert
        assertEquals(1, result.size());
        assertEquals(Order.OrderStatus.CONFIRMED, result.get(0).getStatus());
        verify(orderRepository, times(1)).findByStatus(Order.OrderStatus.CONFIRMED);
    }

    @Test
    void createOrder_WhenValid_ShouldCreateOrder() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setShippingAddress("123 Main St");
        request.setShippingCity("Springfield");
        request.setShippingState("IL");
        request.setShippingZip("62701");
        request.setShippingCountry("USA");

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);
        request.setItems(Arrays.asList(itemRequest));

        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(testCustomer));
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));
        when(productService.reserveStock(1L, 2)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        verify(customerService, times(1)).getCustomerById(1L);
        verify(productService, times(1)).getProductById(1L);
        verify(productService, times(1)).reserveStock(1L, 2);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_WhenCustomerNotFound_ShouldThrowException() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(999L);
        request.setItems(Arrays.asList(new OrderItemRequest()));

        when(customerService.getCustomerById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(request);
        });
        
        assertTrue(exception.getMessage().contains("Customer not found"));
        verify(customerService, times(1)).getCustomerById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WhenProductNotFound_ShouldThrowException() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(999L);
        itemRequest.setQuantity(1);
        request.setItems(Arrays.asList(itemRequest));

        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(testCustomer));
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(request);
        });
        
        assertTrue(exception.getMessage().contains("Product not found"));
        verify(productService, times(1)).getProductById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WhenProductOutOfStock_ShouldThrowException() {
        // Arrange
        testProduct.setStockQuantity(0);
        
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(1);
        request.setItems(Arrays.asList(itemRequest));

        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(testCustomer));
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(request);
        });
        
        assertTrue(exception.getMessage().contains("out of stock"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WhenInsufficientStock_ShouldThrowException() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(200);
        request.setItems(Arrays.asList(itemRequest));

        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(testCustomer));
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));
        when(productService.reserveStock(1L, 200)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(request);
        });
        
        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WhenOrderExists_ShouldUpdateStatus() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.SHIPPED);

        // Assert
        assertNotNull(result);
        assertEquals(Order.OrderStatus.SHIPPED, result.getStatus());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void updateOrderStatus_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.updateOrderStatus(999L, Order.OrderStatus.SHIPPED);
        });
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(orderRepository, times(1)).findById(999L);
    }

    @Test
    void updateOrderStatus_WhenCancelling_ShouldRestoreInventory() {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        // We need to add order items to test inventory restoration
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.CANCELLED);

        // Assert
        assertNotNull(result);
        assertEquals(Order.OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void deleteOrder_WhenOrderExists_ShouldDeleteAndRestoreInventory() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        doNothing().when(orderRepository).deleteById(1L);

        // Act
        orderService.deleteOrder(1L);

        // Assert
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteOrder_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.deleteOrder(999L);
        });
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(orderRepository, times(1)).findById(999L);
        verify(orderRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteOrder_WhenOrderCancelled_ShouldNotRestoreInventory() {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        doNothing().when(orderRepository).deleteById(1L);

        // Act
        orderService.deleteOrder(1L);

        // Assert
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).deleteById(1L);
        // Inventory should not be restored since order was already cancelled
    }
}
