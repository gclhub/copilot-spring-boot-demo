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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
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
    void getAllProducts_noParam_returnsAll() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("WIDGET-001"));
    }

    @Test
    void getAllProducts_activeOnly_returnsActive() throws Exception {
        when(productService.getActiveProducts()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products").param("activeOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void getProductById_found_returns200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getProductById_notFound_returns404() throws Exception {
        when(productService.getProductById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductBySku_found_returns200() throws Exception {
        when(productService.getProductBySku("WIDGET-001")).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/sku/WIDGET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("WIDGET-001"));
    }

    @Test
    void getProductBySku_notFound_returns404() throws Exception {
        when(productService.getProductBySku("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/sku/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductsByCategory_returns200() throws Exception {
        when(productService.getProductsByCategory("General")).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/category/General"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("General"));
    }

    @Test
    void getLowStockProducts_defaultThreshold_returns200() throws Exception {
        when(productService.getLowStockProducts(10)).thenReturn(List.of());

        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk());
    }

    @Test
    void getLowStockProducts_customThreshold_returns200() throws Exception {
        when(productService.getLowStockProducts(5)).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/low-stock").param("threshold", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void createProduct_returns201() throws Exception {
        when(productService.createProduct(any(Product.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("WIDGET-001"));
    }

    @Test
    void updateProduct_found_returns200() throws Exception {
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(product);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateProduct_notFound_returns404() throws Exception {
        when(productService.updateProduct(eq(99L), any(Product.class)))
                .thenThrow(new IllegalArgumentException("Product not found"));

        mockMvc.perform(put("/api/products/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isNotFound());
    }

    @Test
    void reserveStock_success_returns200() throws Exception {
        when(productService.reserveStock(1L, 3)).thenReturn(true);

        mockMvc.perform(post("/api/products/1/reserve").param("quantity", "3"))
                .andExpect(status().isOk());
    }

    @Test
    void reserveStock_insufficientStock_returns400() throws Exception {
        when(productService.reserveStock(1L, 100)).thenReturn(false);

        mockMvc.perform(post("/api/products/1/reserve").param("quantity", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reserveStock_productNotFound_returns404() throws Exception {
        when(productService.reserveStock(99L, 1))
                .thenThrow(new IllegalArgumentException("Product not found"));

        mockMvc.perform(post("/api/products/99/reserve").param("quantity", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void restoreStock_success_returns200() throws Exception {
        mockMvc.perform(post("/api/products/1/restore").param("quantity", "3"))
                .andExpect(status().isOk());
    }

    @Test
    void restoreStock_productNotFound_returns404() throws Exception {
        doThrow(new IllegalArgumentException("Product not found"))
                .when(productService).restoreStock(99L, 3);

        mockMvc.perform(post("/api/products/99/restore").param("quantity", "3"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_returns204() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_notFound_returns404() throws Exception {
        doThrow(new IllegalArgumentException("Product not found"))
                .when(productService).deleteProduct(99L);

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound());
    }
}
