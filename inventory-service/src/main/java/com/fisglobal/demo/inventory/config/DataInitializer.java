package com.fisglobal.demo.inventory.config;

import com.fisglobal.demo.inventory.model.Product;
import com.fisglobal.demo.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing inventory data...");

        if (productRepository.count() == 0) {
            Product product1 = new Product();
            product1.setName("Laptop");
            product1.setDescription("High-performance laptop");
            product1.setSku("LAPTOP-001");
            product1.setPrice(new BigDecimal("999.99"));
            product1.setStockQuantity(50);
            product1.setCategory("Electronics");
            product1.setReorderLevel(10);
            product1.setActive(true);

            Product product2 = new Product();
            product2.setName("Wireless Mouse");
            product2.setDescription("Ergonomic wireless mouse");
            product2.setSku("MOUSE-001");
            product2.setPrice(new BigDecimal("29.99"));
            product2.setStockQuantity(200);
            product2.setCategory("Electronics");
            product2.setReorderLevel(50);
            product2.setActive(true);

            Product product3 = new Product();
            product3.setName("USB-C Cable");
            product3.setDescription("6ft USB-C charging cable");
            product3.setSku("CABLE-001");
            product3.setPrice(new BigDecimal("12.99"));
            product3.setStockQuantity(500);
            product3.setCategory("Accessories");
            product3.setReorderLevel(100);
            product3.setActive(true);

            Product product4 = new Product();
            product4.setName("Mechanical Keyboard");
            product4.setDescription("RGB mechanical gaming keyboard");
            product4.setSku("KEYBOARD-001");
            product4.setPrice(new BigDecimal("149.99"));
            product4.setStockQuantity(75);
            product4.setCategory("Electronics");
            product4.setReorderLevel(20);
            product4.setActive(true);

            Product product5 = new Product();
            product5.setName("Monitor Stand");
            product5.setDescription("Adjustable monitor stand");
            product5.setSku("STAND-001");
            product5.setPrice(new BigDecimal("39.99"));
            product5.setStockQuantity(100);
            product5.setCategory("Accessories");
            product5.setReorderLevel(25);
            product5.setActive(true);

            Product product6 = new Product();
            product6.setName("Webcam");
            product6.setDescription("1080p HD webcam");
            product6.setSku("WEBCAM-001");
            product6.setPrice(new BigDecimal("79.99"));
            product6.setStockQuantity(60);
            product6.setCategory("Electronics");
            product6.setReorderLevel(15);
            product6.setActive(true);

            productRepository.save(product1);
            productRepository.save(product2);
            productRepository.save(product3);
            productRepository.save(product4);
            productRepository.save(product5);
            productRepository.save(product6);

            log.info("Created {} products", productRepository.count());
        }

        log.info("Inventory data initialization complete!");
    }
}
