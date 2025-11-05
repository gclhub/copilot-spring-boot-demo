package com.fisglobal.demo.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisglobal.demo.order.dto.CreateOrderRequest;
import com.fisglobal.demo.order.dto.OrderItemRequest;
import com.fisglobal.demo.order.model.Order;
import com.fisglobal.demo.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private Order testOrder;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomerId(1L);
        testOrder.setOrderNumber("ORD-12345");
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        testOrder.setShippingAddress("123 Main St");
        testOrder.setShippingCity("Springfield");
        testOrder.setShippingState("IL");
        testOrder.setShippingZip("62701");
        testOrder.setShippingCountry("USA");

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomerId(1L);
        createOrderRequest.setShippingAddress("123 Main St");
        createOrderRequest.setShippingCity("Springfield");
        createOrderRequest.setShippingState("IL");
        createOrderRequest.setShippingZip("62701");
        createOrderRequest.setShippingCountry("USA");

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);
        createOrderRequest.setItems(Arrays.asList(itemRequest));
    }

    @Test
    void getAllOrders_ShouldReturnOrderList() throws Exception {
        // Arrange
        when(orderService.getAllOrders()).thenReturn(Arrays.asList(testOrder));

        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-12345"))
                .andExpect(jsonPath("$[0].customerId").value(1));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void getOrderById_WhenExists_ShouldReturnOrder() throws Exception {
        // Arrange
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"));

        verify(orderService, times(1)).getOrderById(1L);
    }

    @Test
    void getOrderById_WhenNotExists_ShouldReturn404() throws Exception {
        // Arrange
        when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrderById(999L);
    }

    @Test
    void getOrderByOrderNumber_WhenExists_ShouldReturnOrder() throws Exception {
        // Arrange
        when(orderService.getOrderByOrderNumber("ORD-12345")).thenReturn(Optional.of(testOrder));

        // Act & Assert
        mockMvc.perform(get("/api/orders/order-number/ORD-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"));

        verify(orderService, times(1)).getOrderByOrderNumber("ORD-12345");
    }

    @Test
    void getOrderByOrderNumber_WhenNotExists_ShouldReturn404() throws Exception {
        // Arrange
        when(orderService.getOrderByOrderNumber("NONEXISTENT")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/orders/order-number/NONEXISTENT"))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrderByOrderNumber("NONEXISTENT");
    }

    @Test
    void getOrdersByCustomerId_ShouldReturnCustomerOrders() throws Exception {
        // Arrange
        when(orderService.getOrdersByCustomerId(1L)).thenReturn(Arrays.asList(testOrder));

        // Act & Assert
        mockMvc.perform(get("/api/orders/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1));

        verify(orderService, times(1)).getOrdersByCustomerId(1L);
    }

    @Test
    void getOrdersByStatus_ShouldReturnOrdersWithStatus() throws Exception {
        // Arrange
        when(orderService.getOrdersByStatus(Order.OrderStatus.CONFIRMED))
                .thenReturn(Arrays.asList(testOrder));

        // Act & Assert
        mockMvc.perform(get("/api/orders/status/CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));

        verify(orderService, times(1)).getOrdersByStatus(Order.OrderStatus.CONFIRMED);
    }

    @Test
    void createOrder_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(testOrder);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"));

        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void createOrder_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid order data"));

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, times(1)).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    void updateOrderStatus_WhenExists_ShouldReturnUpdatedOrder() throws Exception {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.SHIPPED);
        when(orderService.updateOrderStatus(1L, Order.OrderStatus.SHIPPED)).thenReturn(testOrder);

        // Act & Assert
        mockMvc.perform(patch("/api/orders/1/status?status=SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        verify(orderService, times(1)).updateOrderStatus(1L, Order.OrderStatus.SHIPPED);
    }

    @Test
    void updateOrderStatus_WhenNotExists_ShouldReturn404() throws Exception {
        // Arrange
        when(orderService.updateOrderStatus(999L, Order.OrderStatus.SHIPPED))
                .thenThrow(new IllegalArgumentException("Order not found"));

        // Act & Assert
        mockMvc.perform(patch("/api/orders/999/status?status=SHIPPED"))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).updateOrderStatus(999L, Order.OrderStatus.SHIPPED);
    }

    @Test
    void deleteOrder_WhenExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(orderService).deleteOrder(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).deleteOrder(1L);
    }

    @Test
    void deleteOrder_WhenNotExists_ShouldReturn404() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Order not found"))
                .when(orderService).deleteOrder(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/orders/999"))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).deleteOrder(999L);
    }
}
