package com.fisglobal.demo.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisglobal.demo.inventory.model.Product;
import com.fisglobal.demo.inventory.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void getAllProducts_WithoutFilter_ShouldReturnAllProducts() throws Exception {
        // Arrange
        when(productService.getAllProducts()).thenReturn(Arrays.asList(testProduct));

        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].sku").value("TEST-001"));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void getAllProducts_WithActiveOnlyFilter_ShouldReturnActiveProducts() throws Exception {
        // Arrange
        when(productService.getActiveProducts()).thenReturn(Arrays.asList(testProduct));

        // Act & Assert
        mockMvc.perform(get("/api/products?activeOnly=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));

        verify(productService, times(1)).getActiveProducts();
    }

    @Test
    void getProductById_WhenExists_ShouldReturnProduct() throws Exception {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    void getProductById_WhenNotExists_ShouldReturn404() throws Exception {
        // Arrange
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    void getProductBySku_WhenExists_ShouldReturnProduct() throws Exception {
        // Arrange
        when(productService.getProductBySku("TEST-001")).thenReturn(Optional.of(testProduct));

        // Act & Assert
        mockMvc.perform(get("/api/products/sku/TEST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("TEST-001"));

        verify(productService, times(1)).getProductBySku("TEST-001");
    }

    @Test
    void getProductBySku_WhenNotExists_ShouldReturn404() throws Exception {
        // Arrange
        when(productService.getProductBySku("NONEXISTENT")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/products/sku/NONEXISTENT"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductBySku("NONEXISTENT");
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() throws Exception {
        // Arrange
        when(productService.getProductsByCategory("Electronics")).thenReturn(Arrays.asList(testProduct));

        // Act & Assert
        mockMvc.perform(get("/api/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Electronics"));

        verify(productService, times(1)).getProductsByCategory("Electronics");
    }

    @Test
    void getLowStockProducts_WithDefaultThreshold_ShouldReturnLowStockProducts() throws Exception {
        // Arrange
        testProduct.setStockQuantity(5);
        when(productService.getLowStockProducts(10)).thenReturn(Arrays.asList(testProduct));

        // Act & Assert
        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stockQuantity").value(5));

        verify(productService, times(1)).getLowStockProducts(10);
    }

    @Test
    void getLowStockProducts_WithCustomThreshold_ShouldReturnLowStockProducts() throws Exception {
        // Arrange
        when(productService.getLowStockProducts(20)).thenReturn(Arrays.asList(testProduct));

        // Act & Assert
        mockMvc.perform(get("/api/products/low-stock?threshold=20"))
                .andExpect(status().isOk());

        verify(productService, times(1)).getLowStockProducts(20);
    }

    @Test
    void createProduct_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    void updateProduct_WhenExists_ShouldReturnUpdatedProduct() throws Exception {
        // Arrange
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");
        updatedProduct.setSku("TEST-001");
        updatedProduct.setPrice(new BigDecimal("149.99"));
        updatedProduct.setStockQuantity(150);
        updatedProduct.setCategory("Electronics");
        updatedProduct.setActive(true);

        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        // Act & Assert
        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"));

        verify(productService, times(1)).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void updateProduct_WhenNotExists_ShouldReturn404() throws Exception {
        // Arrange
        when(productService.updateProduct(eq(999L), any(Product.class)))
                .thenThrow(new IllegalArgumentException("Product not found"));

        // Act & Assert
        mockMvc.perform(put("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).updateProduct(eq(999L), any(Product.class));
    }

    @Test
    void reserveStock_WhenSuccessful_ShouldReturnOk() throws Exception {
        // Arrange
        when(productService.reserveStock(1L, 10)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/products/1/reserve?quantity=10"))
                .andExpect(status().isOk());

        verify(productService, times(1)).reserveStock(1L, 10);
    }

    @Test
    void reserveStock_WhenInsufficientStock_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(productService.reserveStock(1L, 200)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/products/1/reserve?quantity=200"))
                .andExpect(status().isBadRequest());

        verify(productService, times(1)).reserveStock(1L, 200);
    }

    @Test
    void reserveStock_WhenProductNotFound_ShouldReturn404() throws Exception {
        // Arrange
        when(productService.reserveStock(999L, 10))
                .thenThrow(new IllegalArgumentException("Product not found"));

        // Act & Assert
        mockMvc.perform(post("/api/products/999/reserve?quantity=10"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).reserveStock(999L, 10);
    }

    @Test
    void restoreStock_WhenSuccessful_ShouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(productService).restoreStock(1L, 10);

        // Act & Assert
        mockMvc.perform(post("/api/products/1/restore?quantity=10"))
                .andExpect(status().isOk());

        verify(productService, times(1)).restoreStock(1L, 10);
    }

    @Test
    void restoreStock_WhenProductNotFound_ShouldReturn404() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Product not found"))
                .when(productService).restoreStock(999L, 10);

        // Act & Assert
        mockMvc.perform(post("/api/products/999/restore?quantity=10"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).restoreStock(999L, 10);
    }

    @Test
    void deleteProduct_WhenExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(productService).deleteProduct(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void deleteProduct_WhenNotExists_ShouldReturn404() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Product not found"))
                .when(productService).deleteProduct(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).deleteProduct(999L);
    }
}
