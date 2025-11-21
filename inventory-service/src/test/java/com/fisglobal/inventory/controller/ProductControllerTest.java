package com.fisglobal.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisglobal.inventory.model.Product;
import com.fisglobal.inventory.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("Product Controller Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("Should return all products")
    void shouldReturnAllProducts() throws Exception {
        // Arrange
        Product product1 = createTestProduct(1L, "Laptop", "LAPTOP-001", new BigDecimal("999.99"), 50);
        Product product2 = createTestProduct(2L, "Mouse", "MOUSE-001", new BigDecimal("29.99"), 100);
        List<Product> products = Arrays.asList(product1, product2);

        when(productService.getAllProducts()).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Laptop")))
                .andExpect(jsonPath("$[0].sku", is("LAPTOP-001")))
                .andExpect(jsonPath("$[0].price", is(999.99)))
                .andExpect(jsonPath("$[0].stockQuantity", is(50)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Mouse")));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("Should return product when valid ID")
    void shouldReturnProductWhenValidId() throws Exception {
        // Arrange
        Product product = createTestProduct(1L, "Laptop", "LAPTOP-001", new BigDecimal("999.99"), 50);
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Laptop")))
                .andExpect(jsonPath("$.sku", is("LAPTOP-001")))
                .andExpect(jsonPath("$.price", is(999.99)))
                .andExpect(jsonPath("$.stockQuantity", is(50)))
                .andExpect(jsonPath("$.category", is("Electronics")));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("Should return 404 when invalid ID")
    void shouldReturn404WhenInvalidId() throws Exception {
        // Arrange
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    @DisplayName("Should create product when valid data")
    void shouldCreateProductWhenValidData() throws Exception {
        // Arrange
        Product productToCreate = createTestProduct(null, "Keyboard", "KEYBOARD-001", new BigDecimal("79.99"), 75);
        Product createdProduct = createTestProduct(3L, "Keyboard", "KEYBOARD-001", new BigDecimal("79.99"), 75);

        when(productService.createProduct(any(Product.class))).thenReturn(createdProduct);

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productToCreate)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("Keyboard")))
                .andExpect(jsonPath("$.sku", is("KEYBOARD-001")))
                .andExpect(jsonPath("$.price", is(79.99)))
                .andExpect(jsonPath("$.stockQuantity", is(75)));

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    @DisplayName("Should update product when valid data")
    void shouldUpdateProductWhenValidData() throws Exception {
        // Arrange
        Product updatedDetails = createTestProduct(null, "Laptop Pro", "LAPTOP-001", new BigDecimal("1299.99"), 30);
        Product updatedProduct = createTestProduct(1L, "Laptop Pro", "LAPTOP-001", new BigDecimal("1299.99"), 30);

        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        // Act & Assert
        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Laptop Pro")))
                .andExpect(jsonPath("$.price", is(1299.99)))
                .andExpect(jsonPath("$.stockQuantity", is(30)));

        verify(productService, times(1)).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    @DisplayName("Should delete product when valid ID")
    void shouldDeleteProductWhenValidId() throws Exception {
        // Arrange
        doNothing().when(productService).deleteProduct(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    @DisplayName("Should reserve stock when sufficient quantity")
    void shouldReserveStockWhenSufficientQuantity() throws Exception {
        // Arrange
        when(productService.reserveStock(1L, 10)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/products/1/reserve")
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Stock reserved successfully")));

        verify(productService, times(1)).reserveStock(1L, 10);
    }

    @Test
    @DisplayName("Should return 400 when insufficient stock")
    void shouldReturn400WhenInsufficientStock() throws Exception {
        // Arrange
        when(productService.reserveStock(1L, 1000)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/products/1/reserve")
                        .param("quantity", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Insufficient stock")));

        verify(productService, times(1)).reserveStock(1L, 1000);
    }

    private Product createTestProduct(Long id, String name, String sku, BigDecimal price, Integer stockQuantity) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("Test product description");
        product.setSku(sku);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCategory("Electronics");
        product.setReorderLevel(10);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }
}
