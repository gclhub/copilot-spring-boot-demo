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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    void getActiveProducts_ShouldReturnOnlyActiveProducts() {
        // Given
        List<Product> activeProducts = Arrays.asList(testProduct);
        when(productRepository.findByActiveTrue()).thenReturn(activeProducts);

        // When
        List<Product> result = productService.getActiveProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActive()).isTrue();
        verify(productRepository, times(1)).findByActiveTrue();
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
    void getProductById_WhenProductDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Product> result = productService.getProductById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void getProductBySku_WhenProductExists_ShouldReturnProduct() {
        // Given
        when(productRepository.findBySku("LAPTOP-001")).thenReturn(Optional.of(testProduct));

        // When
        Optional<Product> result = productService.getProductBySku("LAPTOP-001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSku()).isEqualTo("LAPTOP-001");
        verify(productRepository, times(1)).findBySku("LAPTOP-001");
    }

    @Test
    void getProductBySku_WhenProductDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(productRepository.findBySku("UNKNOWN")).thenReturn(Optional.empty());

        // When
        Optional<Product> result = productService.getProductBySku("UNKNOWN");

        // Then
        assertThat(result).isEmpty();
        verify(productRepository, times(1)).findBySku("UNKNOWN");
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategory("Electronics")).thenReturn(products);

        // When
        List<Product> result = productService.getProductsByCategory("Electronics");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Electronics");
        verify(productRepository, times(1)).findByCategory("Electronics");
    }

    @Test
    void getLowStockProducts_ShouldReturnLowStockProducts() {
        // Given
        List<Product> lowStockProducts = Arrays.asList(testProduct);
        when(productRepository.findByStockQuantityLessThanEqual(20)).thenReturn(lowStockProducts);

        // When
        List<Product> result = productService.getLowStockProducts(20);

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository, times(1)).findByStockQuantityLessThanEqual(20);
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
    void createProduct_WhenSkuAlreadyExists_ShouldThrowException() {
        // Given
        when(productRepository.existsBySku(testProduct.getSku())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> productService.createProduct(testProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(productRepository, times(1)).existsBySku(testProduct.getSku());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductExists_ShouldUpdateProduct() {
        // Given
        Product updatedDetails = new Product();
        updatedDetails.setName("Updated Laptop");
        updatedDetails.setDescription("Updated description");
        updatedDetails.setSku("LAPTOP-001");
        updatedDetails.setPrice(new BigDecimal("1399.99"));
        updatedDetails.setStockQuantity(40);
        updatedDetails.setCategory("Electronics");
        updatedDetails.setReorderLevel(15);
        updatedDetails.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.updateProduct(1L, updatedDetails);

        // Then
        assertThat(result).isNotNull();
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        Product updatedDetails = new Product();
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.updateProduct(999L, updatedDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");

        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
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
    void reserveStock_WhenProductNotFound_ShouldThrowException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.reserveStock(999L, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");

        verify(productRepository, times(1)).findById(999L);
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

    @Test
    void restoreStock_WhenProductNotFound_ShouldThrowException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.restoreStock(999L, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");

        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldDeleteProduct() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);

        // When
        productService.deleteProduct(1L);

        // Then
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        when(productRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");

        verify(productRepository, times(1)).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }
}
