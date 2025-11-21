package com.fisglobal.order.config;

import com.fisglobal.order.dto.CreateOrderRequest;
import com.fisglobal.order.dto.OrderItemRequest;
import com.fisglobal.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Initializes the database with sample order data.
 * 
 * Note: This creates orders by calling the OrderService, which in turn
 * calls the Customer Service and Inventory Service to validate data
 * and reserve stock.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final OrderService orderService;

    @Override
    public void run(String... args) {
        try {
            log.info("Initializing order data...");

            // Wait a moment to ensure Customer and Inventory services are available
            Thread.sleep(2000);

            // Create sample order 1 - John Doe orders laptop and mouse
            CreateOrderRequest order1 = new CreateOrderRequest();
            order1.setCustomerId(1L);
            order1.setShippingAddress("123 Main St");
            order1.setShippingCity("New York");
            order1.setShippingState("NY");
            order1.setShippingZip("10001");
            order1.setShippingCountry("USA");

            OrderItemRequest item1 = new OrderItemRequest();
            item1.setProductId(1L);
            item1.setQuantity(1);

            OrderItemRequest item2 = new OrderItemRequest();
            item2.setProductId(2L);
            item2.setQuantity(2);

            order1.setItems(Arrays.asList(item1, item2));

            try {
                orderService.createOrder(order1);
                log.info("Created sample order 1 for customer 1");
            } catch (Exception e) {
                log.warn("Could not create sample order 1: {}", e.getMessage());
            }

            // Create sample order 2 - Jane Smith orders keyboard
            CreateOrderRequest order2 = new CreateOrderRequest();
            order2.setCustomerId(2L);
            order2.setShippingAddress("456 Oak Ave");
            order2.setShippingCity("Los Angeles");
            order2.setShippingState("CA");
            order2.setShippingZip("90001");
            order2.setShippingCountry("USA");

            OrderItemRequest item3 = new OrderItemRequest();
            item3.setProductId(3L);
            item3.setQuantity(1);

            order2.setItems(Arrays.asList(item3));

            try {
                orderService.createOrder(order2);
                log.info("Created sample order 2 for customer 2");
            } catch (Exception e) {
                log.warn("Could not create sample order 2: {}", e.getMessage());
            }

            log.info("Sample order data initialization complete!");

        } catch (InterruptedException e) {
            log.error("Initialization interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error during order data initialization: {}", e.getMessage(), e);
        }
    }
}
