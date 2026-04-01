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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Widget");
        product.setSku("WIDGET-001");
        product.setPrice(new BigDecimal("9.99"));
        product.setStockQuantity(50);
        product.setCategory("General");
        product.setReorderLevel(10);
        product.setActive(true);
    }

    @Test
    void getAllProducts_returnsAll() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> result = productService.getAllProducts();

        assertThat(result).hasSize(1).containsExactly(product);
    }

    @Test
    void getActiveProducts_returnsActiveOnly() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of(product));

        List<Product> result = productService.getActiveProducts();

        assertThat(result).hasSize(1).containsExactly(product);
    }

    @Test
    void getProductById_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> result = productService.getProductById(1L);

        assertThat(result).isPresent().contains(product);
    }

    @Test
    void getProductById_notFound_returnsEmpty() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void getProductBySku_found() {
        when(productRepository.findBySku("WIDGET-001")).thenReturn(Optional.of(product));

        Optional<Product> result = productService.getProductBySku("WIDGET-001");

        assertThat(result).isPresent().contains(product);
    }

    @Test
    void getProductsByCategory_returnsMatching() {
        when(productRepository.findByCategory("General")).thenReturn(List.of(product));

        List<Product> result = productService.getProductsByCategory("General");

        assertThat(result).hasSize(1).containsExactly(product);
    }

    @Test
    void getLowStockProducts_returnsUnderThreshold() {
        when(productRepository.findByStockQuantityLessThanEqual(5)).thenReturn(List.of());

        List<Product> result = productService.getLowStockProducts(5);

        assertThat(result).isEmpty();
    }

    @Test
    void createProduct_success() {
        when(productRepository.existsBySku("WIDGET-001")).thenReturn(false);
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.createProduct(product);

        assertThat(result).isEqualTo(product);
        verify(productRepository).save(product);
    }

    @Test
    void createProduct_duplicateSku_throwsException() {
        when(productRepository.existsBySku("WIDGET-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(product))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("WIDGET-001");

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_success() {
        Product update = new Product();
        update.setName("Updated Widget");
        update.setSku("WIDGET-002");
        update.setPrice(new BigDecimal("19.99"));
        update.setStockQuantity(100);
        update.setCategory("Premium");
        update.setReorderLevel(20);
        update.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.updateProduct(1L, update);

        assertThat(result.getName()).isEqualTo("Updated Widget");
        assertThat(result.getSku()).isEqualTo("WIDGET-002");
    }

    @Test
    void updateProduct_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, product))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reserveStock_success_returnsTrue_andDecrementsQuantity() {
        product.setStockQuantity(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = productService.reserveStock(1L, 3);

        assertThat(result).isTrue();
        assertThat(product.getStockQuantity()).isEqualTo(7);
        verify(productRepository).save(product);
    }

    @Test
    void reserveStock_insufficientStock_returnsFalse() {
        product.setStockQuantity(2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        boolean result = productService.reserveStock(1L, 5);

        assertThat(result).isFalse();
        assertThat(product.getStockQuantity()).isEqualTo(2);
        verify(productRepository, never()).save(any());
    }

    @Test
    void reserveStock_productNotFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.reserveStock(99L, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void restoreStock_success_incrementsQuantity() {
        product.setStockQuantity(5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.restoreStock(1L, 3);

        assertThat(product.getStockQuantity()).isEqualTo(8);
        verify(productRepository).save(product);
    }

    @Test
    void restoreStock_productNotFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.restoreStock(99L, 3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteProduct_success() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_notFound_throwsException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(IllegalArgumentException.class);

        verify(productRepository, never()).deleteById(anyLong());
    }
}
