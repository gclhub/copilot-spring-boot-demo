package com.fisglobal.demo.inventory.service;

import com.fisglobal.demo.inventory.model.Product;
import com.fisglobal.demo.inventory.repository.ProductRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        testProduct.setDescription("Test Description");
        testProduct.setSku("TEST-001");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(100);
        testProduct.setCategory("Electronics");
        testProduct.setReorderLevel(10);
        testProduct.setActive(true);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        Product product2 = new Product();
        product2.setId(2L);
        product2.setSku("TEST-002");
        List<Product> products = Arrays.asList(testProduct, product2);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getActiveProducts_ShouldReturnOnlyActiveProducts() {
        // Arrange
        List<Product> activeProducts = Arrays.asList(testProduct);
        when(productRepository.findByActiveTrue()).thenReturn(activeProducts);

        // Act
        List<Product> result = productService.getActiveProducts();

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
        verify(productRepository, times(1)).findByActiveTrue();
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Optional<Product> result = productService.getProductById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getProductById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void getProductBySku_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findBySku("TEST-001")).thenReturn(Optional.of(testProduct));

        // Act
        Optional<Product> result = productService.getProductBySku("TEST-001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("TEST-001", result.get().getSku());
        verify(productRepository, times(1)).findBySku("TEST-001");
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategory("Electronics")).thenReturn(products);

        // Act
        List<Product> result = productService.getProductsByCategory("Electronics");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getCategory());
        verify(productRepository, times(1)).findByCategory("Electronics");
    }

    @Test
    void getLowStockProducts_ShouldReturnProductsBelowThreshold() {
        // Arrange
        testProduct.setStockQuantity(5);
        List<Product> lowStockProducts = Arrays.asList(testProduct);
        when(productRepository.findByStockQuantityLessThanEqual(10)).thenReturn(lowStockProducts);

        // Act
        List<Product> result = productService.getLowStockProducts(10);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).getStockQuantity() <= 10);
        verify(productRepository, times(1)).findByStockQuantityLessThanEqual(10);
    }

    @Test
    void createProduct_WhenSkuDoesNotExist_ShouldCreateProduct() {
        // Arrange
        when(productRepository.existsBySku(testProduct.getSku())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.createProduct(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals("TEST-001", result.getSku());
        verify(productRepository, times(1)).existsBySku(testProduct.getSku());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void createProduct_WhenSkuExists_ShouldThrowException() {
        // Arrange
        when(productRepository.existsBySku(testProduct.getSku())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(testProduct);
        });
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(productRepository, times(1)).existsBySku(testProduct.getSku());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductExists_ShouldUpdateProduct() {
        // Arrange
        Product updatedDetails = new Product();
        updatedDetails.setName("Updated Product");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setSku("TEST-001-UPD");
        updatedDetails.setPrice(new BigDecimal("149.99"));
        updatedDetails.setStockQuantity(150);
        updatedDetails.setCategory("Electronics");
        updatedDetails.setReorderLevel(20);
        updatedDetails.setActive(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.updateProduct(1L, updatedDetails);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals(new BigDecimal("149.99"), result.getPrice());
        assertEquals(150, result.getStockQuantity());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void updateProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(999L, testProduct);
        });
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void reserveStock_WhenSufficientStock_ShouldReserveAndReturnTrue() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        boolean result = productService.reserveStock(1L, 50);

        // Assert
        assertTrue(result);
        assertEquals(50, testProduct.getStockQuantity());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void reserveStock_WhenInsufficientStock_ShouldReturnFalse() {
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
    void reserveStock_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.reserveStock(999L, 10);
        });
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void restoreStock_WhenProductExists_ShouldRestoreStock() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        productService.restoreStock(1L, 25);

        // Assert
        assertEquals(125, testProduct.getStockQuantity());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void restoreStock_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.restoreStock(999L, 10);
        });
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldDeleteProduct() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.deleteProduct(999L);
        });
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(productRepository, times(1)).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }
}
