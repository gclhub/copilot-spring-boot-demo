package com.fisglobal.inventory.service;

import com.fisglobal.inventory.model.Product;
import com.fisglobal.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 * Tests follow AAA (Arrange-Act-Assert) pattern and use JUnit 5 best practices.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setSku("TEST-001");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(100);
        testProduct.setCategory("Electronics");
        testProduct.setReorderLevel(10);
        testProduct.setActive(true);
    }

    @Nested
    @DisplayName("Stock Management Tests")
    class StockManagementTests {

        @Test
        @DisplayName("Should reserve stock when sufficient quantity available")
        void shouldReserveStockWhenSufficientQuantityAvailable() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // Act
            boolean result = productService.reserveStock(1L, 10);

            // Assert
            assertTrue(result);
            assertEquals(90, testProduct.getStockQuantity());
            verify(productRepository, times(1)).findById(1L);
            verify(productRepository, times(1)).save(testProduct);
        }

        @Test
        @DisplayName("Should not reserve stock when insufficient quantity available")
        void shouldNotReserveStockWhenInsufficientQuantityAvailable() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            // Act
            boolean result = productService.reserveStock(1L, 150);

            // Assert
            assertFalse(result);
            assertEquals(100, testProduct.getStockQuantity());
            verify(productRepository, times(1)).findById(1L);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when reserving stock for non-existent product")
        void shouldThrowExceptionWhenReservingStockForNonExistentProduct() {
            // Arrange
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> productService.reserveStock(999L, 10)
            );
            verify(productRepository, times(1)).findById(999L);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should restore stock when valid data is provided")
        void shouldRestoreStockWhenValidDataIsProvided() {
            // Arrange
            testProduct.setStockQuantity(50);
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // Act
            productService.restoreStock(1L, 20);

            // Assert
            assertEquals(70, testProduct.getStockQuantity());
            verify(productRepository, times(1)).findById(1L);
            verify(productRepository, times(1)).save(testProduct);
        }

        @Test
        @DisplayName("Should throw exception when restoring stock for non-existent product")
        void shouldThrowExceptionWhenRestoringStockForNonExistentProduct() {
            // Arrange
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> productService.restoreStock(999L, 10)
            );
            verify(productRepository, times(1)).findById(999L);
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product when valid data is provided")
        void shouldCreateProductWhenValidDataIsProvided() {
            // Arrange
            when(productRepository.existsBySku(testProduct.getSku())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // Act
            Product result = productService.createProduct(testProduct);

            // Assert
            assertNotNull(result);
            assertEquals(testProduct.getSku(), result.getSku());
            verify(productRepository, times(1)).existsBySku(testProduct.getSku());
            verify(productRepository, times(1)).save(testProduct);
        }

        @Test
        @DisplayName("Should throw exception when SKU already exists")
        void shouldThrowExceptionWhenSkuAlreadyExists() {
            // Arrange
            when(productRepository.existsBySku(testProduct.getSku())).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> productService.createProduct(testProduct)
            );
            assertTrue(exception.getMessage().contains("already exists"));
            verify(productRepository, times(1)).existsBySku(testProduct.getSku());
            verify(productRepository, never()).save(any(Product.class));
        }
    }
}
