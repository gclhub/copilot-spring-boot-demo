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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
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

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(100L);
        order.setCustomerId(1L);
        order.setOrderNumber("ORD-12345");
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setItems(new ArrayList<>());
    }

    @Test
    void getAllOrders_returns200() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-12345"));
    }

    @Test
    void getOrderById_found_returns200() throws Exception {
        when(orderService.getOrderById(100L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/orders/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void getOrderById_notFound_returns404() throws Exception {
        when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderByOrderNumber_found_returns200() throws Exception {
        when(orderService.getOrderByOrderNumber("ORD-12345")).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/orders/order-number/ORD-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"));
    }

    @Test
    void getOrderByOrderNumber_notFound_returns404() throws Exception {
        when(orderService.getOrderByOrderNumber("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/order-number/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrdersByCustomerId_returns200() throws Exception {
        when(orderService.getOrdersByCustomerId(1L)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1));
    }

    @Test
    void getOrdersByStatus_returns200() throws Exception {
        when(orderService.getOrdersByStatus(Order.OrderStatus.CONFIRMED)).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders/status/CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    void createOrder_success_returns201() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                1L,
                List.of(new OrderItemRequest(10L, 2)),
                "123 Main St", "Springfield", "IL", "62701", "US");

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"));
    }

    @Test
    void createOrder_invalidRequest_returns400() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                1L,
                List.of(new OrderItemRequest(10L, 2)),
                null, null, null, null, null);

        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new IllegalArgumentException("Customer not found"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateOrderStatus_found_returns200() throws Exception {
        when(orderService.updateOrderStatus(100L, Order.OrderStatus.CANCELLED))
                .thenReturn(order);

        mockMvc.perform(patch("/api/orders/100/status").param("status", "CANCELLED"))
                .andExpect(status().isOk());
    }

    @Test
    void updateOrderStatus_notFound_returns404() throws Exception {
        when(orderService.updateOrderStatus(eq(999L), any(Order.OrderStatus.class)))
                .thenThrow(new IllegalArgumentException("Order not found"));

        mockMvc.perform(patch("/api/orders/999/status").param("status", "CANCELLED"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrder_returns204() throws Exception {
        mockMvc.perform(delete("/api/orders/100"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteOrder_notFound_returns404() throws Exception {
        doThrow(new IllegalArgumentException("Order not found"))
                .when(orderService).deleteOrder(999L);

        mockMvc.perform(delete("/api/orders/999"))
                .andExpect(status().isNotFound());
    }
}
