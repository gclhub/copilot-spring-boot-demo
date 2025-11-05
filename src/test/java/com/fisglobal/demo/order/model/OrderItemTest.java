package com.fisglobal.demo.order.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProductId(1L);
        orderItem.setProductName("Test Product");
        orderItem.setProductSku("TEST-001");
        orderItem.setQuantity(3);
        orderItem.setUnitPrice(new BigDecimal("25.00"));
    }

    @Test
    void calculateSubtotal_ShouldMultiplyQuantityByUnitPrice() {
        // Act
        orderItem.calculateSubtotal();

        // Assert
        assertEquals(new BigDecimal("75.00"), orderItem.getSubtotal());
    }

    @Test
    void calculateSubtotal_WithDecimalPrice_ShouldCalculateCorrectly() {
        // Arrange
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(new BigDecimal("19.99"));

        // Act
        orderItem.calculateSubtotal();

        // Assert
        assertEquals(new BigDecimal("39.98"), orderItem.getSubtotal());
    }

    @Test
    void getSubtotal_WhenNotCalculated_ShouldCalculateAndReturn() {
        // Arrange
        orderItem.setQuantity(4);
        orderItem.setUnitPrice(new BigDecimal("10.50"));

        // Act
        BigDecimal result = orderItem.getSubtotal();

        // Assert
        assertEquals(new BigDecimal("42.00"), result);
    }

    @Test
    void getSubtotal_WhenAlreadyCalculated_ShouldReturnExisting() {
        // Arrange
        orderItem.calculateSubtotal();
        BigDecimal expectedSubtotal = orderItem.getSubtotal();

        // Act
        BigDecimal result = orderItem.getSubtotal();

        // Assert
        assertEquals(expectedSubtotal, result);
    }

    @Test
    void calculateSubtotal_WithSingleQuantity_ShouldEqualUnitPrice() {
        // Arrange
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(new BigDecimal("100.00"));

        // Act
        orderItem.calculateSubtotal();

        // Assert
        assertEquals(new BigDecimal("100.00"), orderItem.getSubtotal());
    }

    @Test
    void calculateSubtotal_WithZeroQuantity_ShouldReturnZero() {
        // Arrange
        orderItem.setQuantity(0);
        orderItem.setUnitPrice(new BigDecimal("50.00"));

        // Act
        orderItem.calculateSubtotal();

        // Assert
        assertEquals(new BigDecimal("0.00"), orderItem.getSubtotal());
    }

    @Test
    void calculateSubtotal_WhenQuantityIsNull_ShouldNotThrowException() {
        // Arrange
        orderItem.setQuantity(null);
        orderItem.setUnitPrice(new BigDecimal("50.00"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> orderItem.calculateSubtotal());
    }

    @Test
    void calculateSubtotal_WhenUnitPriceIsNull_ShouldNotThrowException() {
        // Arrange
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(null);

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> orderItem.calculateSubtotal());
    }
}
