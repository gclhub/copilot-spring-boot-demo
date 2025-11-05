package com.fisglobal.demo.inventory.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setSku("TEST-001");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(100);
        product.setCategory("Electronics");
        product.setReorderLevel(10);
        product.setActive(true);
    }

    @Test
    void needsReorder_WhenStockBelowReorderLevel_ShouldReturnTrue() {
        // Arrange
        product.setStockQuantity(5);
        product.setReorderLevel(10);

        // Act
        boolean result = product.needsReorder();

        // Assert
        assertTrue(result);
    }

    @Test
    void needsReorder_WhenStockEqualsReorderLevel_ShouldReturnTrue() {
        // Arrange
        product.setStockQuantity(10);
        product.setReorderLevel(10);

        // Act
        boolean result = product.needsReorder();

        // Assert
        assertTrue(result);
    }

    @Test
    void needsReorder_WhenStockAboveReorderLevel_ShouldReturnFalse() {
        // Arrange
        product.setStockQuantity(20);
        product.setReorderLevel(10);

        // Act
        boolean result = product.needsReorder();

        // Assert
        assertFalse(result);
    }

    @Test
    void needsReorder_WhenReorderLevelIsNull_ShouldReturnFalse() {
        // Arrange
        product.setStockQuantity(5);
        product.setReorderLevel(null);

        // Act
        boolean result = product.needsReorder();

        // Assert
        assertFalse(result);
    }

    @Test
    void isInStock_WhenStockGreaterThanZero_ShouldReturnTrue() {
        // Arrange
        product.setStockQuantity(10);

        // Act
        boolean result = product.isInStock();

        // Assert
        assertTrue(result);
    }

    @Test
    void isInStock_WhenStockIsZero_ShouldReturnFalse() {
        // Arrange
        product.setStockQuantity(0);

        // Act
        boolean result = product.isInStock();

        // Assert
        assertFalse(result);
    }

    @Test
    void onCreate_ShouldSetDefaultActiveToTrue() {
        // Arrange
        Product newProduct = new Product();
        newProduct.setActive(null);

        // Act
        newProduct.onCreate();

        // Assert
        assertTrue(newProduct.getActive());
        assertNotNull(newProduct.getCreatedAt());
        assertNotNull(newProduct.getUpdatedAt());
    }

    @Test
    void onUpdate_ShouldUpdateTimestamp() {
        // Arrange
        product.onCreate();
        
        // Act
        product.onUpdate();

        // Assert
        assertNotNull(product.getUpdatedAt());
    }
}
