package com.fisglobal.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisglobal.order.client.CustomerClient;
import com.fisglobal.order.client.InventoryClient;
import com.fisglobal.order.config.ServiceConfig;
import com.fisglobal.order.dto.CreateOrderRequest;
import com.fisglobal.order.dto.OrderItemRequest;
import com.fisglobal.order.model.Order;
import com.fisglobal.order.model.OrderItem;
import com.fisglobal.order.repository.OrderRepository;
import com.fisglobal.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("Order Controller Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private CustomerClient customerClient;

    @MockBean
    private InventoryClient inventoryClient;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private ServiceConfig serviceConfig;

    @Test
    @DisplayName("Should return all orders")
    void shouldReturnAllOrders() throws Exception {
        // Arrange
        Order order1 = createTestOrder(1L, 1L, Order.OrderStatus.CONFIRMED, new BigDecimal("999.99"));
        Order order2 = createTestOrder(2L, 2L, Order.OrderStatus.PENDING, new BigDecimal("299.99"));
        List<Order> orders = Arrays.asList(order1, order2);

        when(orderService.getAllOrders()).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].customerId", is(1)))
                .andExpect(jsonPath("$[0].status", is("CONFIRMED")))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    @DisplayName("Should return order when valid ID")
    void shouldReturnOrderWhenValidId() throws Exception {
        // Arrange
        Order order = createTestOrder(1L, 1L, Order.OrderStatus.CONFIRMED, new BigDecimal("999.99"));
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customerId", is(1)))
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.totalAmount", is(999.98)));

        verify(orderService, times(1)).getOrderById(1L);
    }

    @Test
    @DisplayName("Should return 404 when invalid ID")
    void shouldReturn404WhenInvalidId() throws Exception {
        // Arrange
        when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrderById(999L);
    }

    @Test
    @DisplayName("Should create order when valid data")
    void shouldCreateOrderWhenValidData() throws Exception {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest(
                1L,
                Collections.singletonList(itemRequest),
                "123 Main St",
                "New York",
                "NY",
                "10001",
                "USA"
        );

        Order createdOrder = createTestOrder(1L, 1L, Order.OrderStatus.CONFIRMED, new BigDecimal("999.99"));
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(createdOrder);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customerId", is(1)))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    @DisplayName("Should update order status when valid")
    void shouldUpdateOrderStatusWhenValid() throws Exception {
        // Arrange
        Order updatedOrder = createTestOrder(1L, 1L, Order.OrderStatus.SHIPPED, new BigDecimal("999.99"));
        when(orderService.updateOrderStatus(eq(1L), eq(Order.OrderStatus.SHIPPED)))
                .thenReturn(updatedOrder);

        // Act & Assert
        mockMvc.perform(patch("/api/orders/1/status")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("SHIPPED")));

        verify(orderService, times(1)).updateOrderStatus(eq(1L), eq(Order.OrderStatus.SHIPPED));
    }

    @Test
    @DisplayName("Should cancel order when valid ID")
    void shouldCancelOrderWhenValidId() throws Exception {
        // Arrange
        Order cancelledOrder = createTestOrder(1L, 1L, Order.OrderStatus.CANCELLED, new BigDecimal("999.99"));
        when(orderService.updateOrderStatus(eq(1L), eq(Order.OrderStatus.CANCELLED)))
                .thenReturn(cancelledOrder);

        // Act & Assert
        mockMvc.perform(patch("/api/orders/1/status")
                        .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        verify(orderService, times(1)).updateOrderStatus(eq(1L), eq(Order.OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("Should return 400 when invalid order request")
    void shouldReturn400WhenInvalidOrderRequest() throws Exception {
        // Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid order data"));

        OrderItemRequest itemRequest = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest(
                1L,
                Collections.singletonList(itemRequest),
                "123 Main St",
                "New York",
                "NY",
                "10001",
                "USA"
        );

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    private Order createTestOrder(Long id, Long customerId, Order.OrderStatus status, BigDecimal totalAmount) {
        Order order = new Order();
        order.setId(id);
        order.setCustomerId(customerId);
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setStatus(status);
        order.setTotalAmount(totalAmount);
        order.setShippingAddress("123 Main St");
        order.setShippingCity("New York");
        order.setShippingState("NY");
        order.setShippingZip("10001");
        order.setShippingCountry("USA");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(1L);
        item.setProductName("Test Product");
        item.setProductSku("TEST-001");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("499.99"));
        order.addItem(item);

        return order;
    }
}
