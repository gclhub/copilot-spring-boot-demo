package com.fisglobal.demo.inventory.config;

import com.fisglobal.demo.inventory.model.Product;
import com.fisglobal.demo.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Data initialization configuration for Inventory Service.
 * Loads sample product data into the database on application startup.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class InventoryDataInitializer {

    @Bean
    CommandLineRunner initInventoryDatabase(ProductRepository productRepository) {
        return args -> {
            log.info("Initializing inventory database with sample data...");

            // Create sample products
            if (productRepository.count() == 0) {
                log.info("Creating sample products...");

                Product product1 = new Product();
                product1.setName("Laptop Computer");
                product1.setDescription("High-performance laptop for business and gaming");
                product1.setSku("LAPTOP-001");
                product1.setPrice(new BigDecimal("1299.99"));
                product1.setStockQuantity(50);
                product1.setCategory("Electronics");
                product1.setReorderLevel(10);
                productRepository.save(product1);

                Product product2 = new Product();
                product2.setName("Wireless Mouse");
                product2.setDescription("Ergonomic wireless mouse with long battery life");
                product2.setSku("MOUSE-001");
                product2.setPrice(new BigDecimal("29.99"));
                product2.setStockQuantity(200);
                product2.setCategory("Electronics");
                product2.setReorderLevel(20);
                productRepository.save(product2);

                Product product3 = new Product();
                product3.setName("Mechanical Keyboard");
                product3.setDescription("RGB mechanical keyboard with cherry switches");
                product3.setSku("KEYBOARD-001");
                product3.setPrice(new BigDecimal("149.99"));
                product3.setStockQuantity(75);
                product3.setCategory("Electronics");
                product3.setReorderLevel(15);
                productRepository.save(product3);

                Product product4 = new Product();
                product4.setName("Office Chair");
                product4.setDescription("Ergonomic office chair with lumbar support");
                product4.setSku("CHAIR-001");
                product4.setPrice(new BigDecimal("299.99"));
                product4.setStockQuantity(30);
                product4.setCategory("Furniture");
                product4.setReorderLevel(5);
                productRepository.save(product4);

                Product product5 = new Product();
                product5.setName("Standing Desk");
                product5.setDescription("Adjustable height standing desk");
                product5.setSku("DESK-001");
                product5.setPrice(new BigDecimal("599.99"));
                product5.setStockQuantity(20);
                product5.setCategory("Furniture");
                product5.setReorderLevel(3);
                productRepository.save(product5);

                Product product6 = new Product();
                product6.setName("Webcam HD");
                product6.setDescription("1080p webcam with built-in microphone");
                product6.setSku("WEBCAM-001");
                product6.setPrice(new BigDecimal("79.99"));
                product6.setStockQuantity(100);
                product6.setCategory("Electronics");
                product6.setReorderLevel(10);
                productRepository.save(product6);

                log.info("Created {} products", productRepository.count());
            }

            log.info("Inventory database initialization complete!");
        };
    }
}
