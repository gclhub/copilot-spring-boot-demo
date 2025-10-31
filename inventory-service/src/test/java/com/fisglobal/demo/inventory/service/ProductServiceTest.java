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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 */
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
        testProduct.setName("Laptop Computer");
        testProduct.setDescription("High-performance laptop");
        testProduct.setSku("LAPTOP-001");
        testProduct.setPrice(new BigDecimal("1299.99"));
        testProduct.setStockQuantity(50);
        testProduct.setCategory("Electronics");
        testProduct.setReorderLevel(10);
        testProduct.setActive(true);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.getAllProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSku()).isEqualTo("LAPTOP-001");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        Optional<Product> result = productService.getProductById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSku()).isEqualTo("LAPTOP-001");
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void createProduct_WhenSkuIsUnique_ShouldSaveProduct() {
        // Given
        when(productRepository.existsBySku(testProduct.getSku())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.createProduct(testProduct);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("LAPTOP-001");
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void reserveStock_WhenSufficientStock_ShouldReturnTrue() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        boolean result = productService.reserveStock(1L, 10);

        // Then
        assertThat(result).isTrue();
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void reserveStock_WhenInsufficientStock_ShouldReturnFalse() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        boolean result = productService.reserveStock(1L, 100);

        // Then
        assertThat(result).isFalse();
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void restoreStock_ShouldIncreaseStockQuantity() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        productService.restoreStock(1L, 10);

        // Then
        verify(productRepository, times(1)).save(any(Product.class));
    }
}
