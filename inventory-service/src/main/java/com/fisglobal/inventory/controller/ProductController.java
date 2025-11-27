package com.fisglobal.inventory.controller;

import com.fisglobal.inventory.model.Product;
import com.fisglobal.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Product/Inventory operations.
 * Exposes endpoints for inventory management.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(@RequestParam(required = false) Boolean activeOnly) {
        List<Product> products = activeOnly != null && activeOnly 
                ? productService.getActiveProducts() 
                : productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        return productService.getProductBySku(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(@RequestParam(defaultValue = "10") Integer threshold) {
        return ResponseEntity.ok(productService.getLowStockProducts(threshold));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, 
                                                 @Valid @RequestBody Product product) {
        try {
            Product updatedProduct = productService.updateProduct(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint for reserving stock.
     * Used by other microservices (e.g., Order Service) to reserve inventory.
     */
    @PostMapping("/{id}/reserve")
    public ResponseEntity<Map<String, Object>> reserveStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            boolean success = productService.reserveStock(id, quantity);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Stock reserved successfully"));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Insufficient stock"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint for restoring stock.
     * Used by other microservices (e.g., Order Service) to restore inventory on order cancellation.
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            productService.restoreStock(id, quantity);
            return ResponseEntity.ok(Map.of("success", true, "message", "Stock restored successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
