package com.fisglobal.inventory.repository;

import com.fisglobal.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity.
 * Provides data access methods for inventory operations.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    List<Product> findByCategory(String category);
    
    List<Product> findByActiveTrue();
    
    List<Product> findByStockQuantityLessThanEqual(Integer quantity);
    
    boolean existsBySku(String sku);
}
