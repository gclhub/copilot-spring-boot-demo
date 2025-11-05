package com.fisglobal.demo.order.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setStatus(Order.OrderStatus.PENDING);

        orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProductId(1L);
        orderItem.setProductName("Test Product");
        orderItem.setProductSku("TEST-001");
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(new BigDecimal("50.00"));
    }

    @Test
    void addItem_ShouldAddItemAndSetOrder() {
        // Act
        order.addItem(orderItem);

        // Assert
        assertEquals(1, order.getItems().size());
        assertEquals(order, orderItem.getOrder());
        assertEquals(new BigDecimal("100.00"), order.getTotalAmount());
    }

    @Test
    void addItem_WithMultipleItems_ShouldCalculateTotalCorrectly() {
        // Arrange
        OrderItem secondItem = new OrderItem();
        secondItem.setId(2L);
        secondItem.setProductId(2L);
        secondItem.setQuantity(3);
        secondItem.setUnitPrice(new BigDecimal("30.00"));

        // Act
        order.addItem(orderItem);
        order.addItem(secondItem);

        // Assert
        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("190.00"), order.getTotalAmount());
    }

    @Test
    void removeItem_ShouldRemoveItemAndRecalculateTotal() {
        // Arrange
        order.addItem(orderItem);

        // Act
        order.removeItem(orderItem);

        // Assert
        assertEquals(0, order.getItems().size());
        assertNull(orderItem.getOrder());
        assertEquals(BigDecimal.ZERO, order.getTotalAmount());
    }

    @Test
    void calculateTotalAmount_WithNoItems_ShouldReturnZero() {
        // Act
        order.calculateTotalAmount();

        // Assert
        assertEquals(BigDecimal.ZERO, order.getTotalAmount());
    }

    @Test
    void calculateTotalAmount_WithItems_ShouldCalculateCorrectly() {
        // Arrange
        order.getItems().add(orderItem);
        orderItem.setOrder(order);
        orderItem.calculateSubtotal();

        OrderItem secondItem = new OrderItem();
        secondItem.setQuantity(1);
        secondItem.setUnitPrice(new BigDecimal("25.00"));
        secondItem.calculateSubtotal();
        order.getItems().add(secondItem);
        secondItem.setOrder(order);

        // Act
        order.calculateTotalAmount();

        // Assert
        assertEquals(new BigDecimal("125.00"), order.getTotalAmount());
    }

    @Test
    void onCreate_ShouldSetTimestampsAndGenerateOrderNumber() {
        // Arrange
        Order newOrder = new Order();

        // Act
        newOrder.onCreate();

        // Assert
        assertNotNull(newOrder.getOrderNumber());
        assertTrue(newOrder.getOrderNumber().startsWith("ORD-"));
        assertNotNull(newOrder.getCreatedAt());
        assertNotNull(newOrder.getUpdatedAt());
    }

    @Test
    void onCreate_WhenOrderNumberAlreadySet_ShouldNotOverwrite() {
        // Arrange
        Order newOrder = new Order();
        newOrder.setOrderNumber("CUSTOM-123");

        // Act
        newOrder.onCreate();

        // Assert
        assertEquals("CUSTOM-123", newOrder.getOrderNumber());
    }

    @Test
    void onUpdate_ShouldUpdateTimestamp() {
        // Arrange
        order.onCreate();

        // Act
        order.onUpdate();

        // Assert
        assertNotNull(order.getUpdatedAt());
    }
}
