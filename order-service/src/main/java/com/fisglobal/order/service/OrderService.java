package com.fisglobal.order.service;

import com.fisglobal.order.client.CustomerClient;
import com.fisglobal.order.client.InventoryClient;
import com.fisglobal.order.dto.CreateOrderRequest;
import com.fisglobal.order.dto.OrderItemRequest;
import com.fisglobal.order.model.Order;
import com.fisglobal.order.model.OrderItem;
import com.fisglobal.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for Order operations.
 * Contains business logic for order management and coordinates with Customer and Inventory services.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final InventoryClient inventoryClient;

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        log.debug("Fetching all orders");
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long id) {
        log.debug("Fetching order with id: {}", id);
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrderByOrderNumber(String orderNumber) {
        log.debug("Fetching order with order number: {}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(Long customerId) {
        log.debug("Fetching orders for customer: {}", customerId);
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        log.debug("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status);
    }

    /**
     * Create a new order.
     * This method coordinates with Customer and Inventory services.
     * Implements compensating transactions for rollback scenarios.
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.debug("Creating new order for customer: {}", request.getCustomerId());

        // Step 1: Validate customer exists via Customer Service
        log.debug("Validating customer {} via Customer Service", request.getCustomerId());
        boolean customerExists = customerClient.validateCustomer(request.getCustomerId());
        
        if (!customerExists) {
            throw new IllegalArgumentException(
                    "Customer not found with id: " + request.getCustomerId());
        }

        // Step 2: Create order
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingZip(request.getShippingZip());
        order.setShippingCountry(request.getShippingCountry());

        // Step 3: Process each order item and reserve inventory
        List<Long> reservedProductIds = new ArrayList<>();
        
        try {
            for (OrderItemRequest itemRequest : request.getItems()) {
                // Get product details from Inventory Service
                log.debug("Fetching product {} from Inventory Service", itemRequest.getProductId());
                Map<String, Object> product = inventoryClient.getProduct(itemRequest.getProductId());
                
                if (product == null) {
                    throw new IllegalArgumentException(
                            "Product not found with id: " + itemRequest.getProductId());
                }

                // Check if product is in stock
                Integer stockQuantity = (Integer) product.get("stockQuantity");
                if (stockQuantity == null || stockQuantity <= 0) {
                    throw new IllegalArgumentException(
                            "Product " + product.get("name") + " is out of stock");
                }

                // Reserve inventory via Inventory Service
                log.debug("Reserving {} units of product {}", 
                         itemRequest.getQuantity(), itemRequest.getProductId());
                boolean reserved = inventoryClient.reserveStock(
                        itemRequest.getProductId(), itemRequest.getQuantity());
                
                if (!reserved) {
                    throw new IllegalArgumentException(
                            "Insufficient stock for product: " + product.get("name"));
                }

                reservedProductIds.add(itemRequest.getProductId());

                // Create order item
                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(itemRequest.getProductId());
                orderItem.setProductName((String) product.get("name"));
                orderItem.setProductSku((String) product.get("sku"));
                orderItem.setQuantity(itemRequest.getQuantity());
                
                // Handle price conversion (could be Integer or Double from JSON)
                Object priceObj = product.get("price");
                BigDecimal unitPrice;
                if (priceObj instanceof Integer) {
                    unitPrice = new BigDecimal((Integer) priceObj);
                } else if (priceObj instanceof Double) {
                    unitPrice = BigDecimal.valueOf((Double) priceObj);
                } else {
                    unitPrice = new BigDecimal(priceObj.toString());
                }
                orderItem.setUnitPrice(unitPrice);
                
                order.addItem(orderItem);
            }

            // Step 4: Set order status and save
            order.setStatus(Order.OrderStatus.CONFIRMED);
            Order savedOrder = orderRepository.save(order);
            
            log.info("Created order {} for customer {}", 
                    savedOrder.getOrderNumber(), request.getCustomerId());
            return savedOrder;
            
        } catch (Exception e) {
            // Compensating transaction: Restore all reserved inventory
            log.error("Order creation failed, restoring inventory for {} products", 
                     reservedProductIds.size());
            
            for (Long productId : reservedProductIds) {
                try {
                    // Find the quantity that was reserved for this product
                    Optional<OrderItemRequest> itemRequest = request.getItems().stream()
                            .filter(item -> item.getProductId().equals(productId))
                            .findFirst();
                    
                    if (itemRequest.isPresent()) {
                        inventoryClient.restoreStock(productId, itemRequest.get().getQuantity());
                    }
                } catch (Exception restoreException) {
                    log.error("Failed to restore stock for product {}: {}", 
                             productId, restoreException.getMessage());
                }
            }
            
            throw e;
        }
    }

    /**
     * Update order status.
     * If order is cancelled, restore inventory via Inventory Service.
     */
    @Transactional
    public Order updateOrderStatus(Long id, Order.OrderStatus newStatus) {
        log.debug("Updating order {} status to {}", id, newStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        // If order is cancelled, restore inventory via Inventory Service
        if (newStatus == Order.OrderStatus.CANCELLED && oldStatus != Order.OrderStatus.CANCELLED) {
            log.info("Cancelling order {}, restoring inventory", order.getOrderNumber());
            for (OrderItem item : order.getItems()) {
                try {
                    inventoryClient.restoreStock(item.getProductId(), item.getQuantity());
                } catch (Exception e) {
                    log.error("Failed to restore stock for product {}: {}", 
                             item.getProductId(), e.getMessage());
                }
            }
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Updated order {} status from {} to {}", 
                savedOrder.getOrderNumber(), oldStatus, newStatus);
        
        return savedOrder;
    }

    /**
     * Delete an order.
     * Restores inventory if order was not cancelled.
     */
    @Transactional
    public void deleteOrder(Long id) {
        log.debug("Deleting order with id: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        // Restore inventory if order was not cancelled
        if (order.getStatus() != Order.OrderStatus.CANCELLED) {
            log.info("Restoring inventory for deleted order {}", order.getOrderNumber());
            for (OrderItem item : order.getItems()) {
                try {
                    inventoryClient.restoreStock(item.getProductId(), item.getQuantity());
                } catch (Exception e) {
                    log.error("Failed to restore stock for product {}: {}", 
                             item.getProductId(), e.getMessage());
                }
            }
        }

        orderRepository.deleteById(id);
        log.info("Deleted order {}", order.getOrderNumber());
    }
}
