package com.fisglobal.demo.order.service;

import com.fisglobal.demo.order.client.CustomerDTO;
import com.fisglobal.demo.order.client.CustomerServiceClient;
import com.fisglobal.demo.order.client.InventoryServiceClient;
import com.fisglobal.demo.order.client.ProductDTO;
import com.fisglobal.demo.order.dto.CreateOrderRequest;
import com.fisglobal.demo.order.dto.OrderItemRequest;
import com.fisglobal.demo.order.model.Order;
import com.fisglobal.demo.order.model.OrderItem;
import com.fisglobal.demo.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Order operations.
 * Contains business logic for order management.
 * 
 * This service communicates with Customer and Inventory services via REST APIs
 * to implement microservices architecture patterns.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerServiceClient customerServiceClient;
    private final InventoryServiceClient inventoryServiceClient;

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
     * This method uses REST clients to validate customer and manage inventory.
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.debug("Creating new order for customer: {}", request.getCustomerId());

        // Validate customer exists via REST call to Customer Service
        CustomerDTO customer = customerServiceClient.getCustomerById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer not found with id: " + request.getCustomerId()));

        // Create order
        Order order = new Order();
        order.setCustomerId(customer.getId());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingZip(request.getShippingZip());
        order.setShippingCountry(request.getShippingCountry());

        // Track items for potential rollback
        List<OrderItem> processedItems = new ArrayList<>();
        
        try {
            // Process each order item via REST call to Inventory Service
            for (OrderItemRequest itemRequest : request.getItems()) {
                ProductDTO product = inventoryServiceClient.getProductById(itemRequest.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Product not found with id: " + itemRequest.getProductId()));

                if (product.getStockQuantity() <= 0) {
                    throw new IllegalArgumentException(
                            "Product " + product.getName() + " is out of stock");
                }

                // Reserve inventory via REST call
                boolean reserved = inventoryServiceClient.reserveStock(
                        product.getId(), itemRequest.getQuantity());
                
                if (!reserved) {
                    throw new IllegalArgumentException(
                            "Insufficient stock for product: " + product.getName());
                }

                // Create order item
                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(product.getId());
                orderItem.setProductName(product.getName());
                orderItem.setProductSku(product.getSku());
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setUnitPrice(product.getPrice());
                
                order.addItem(orderItem);
                processedItems.add(orderItem);
            }

            order.setStatus(Order.OrderStatus.CONFIRMED);
            Order savedOrder = orderRepository.save(order);
            
            log.info("Created order {} for customer {}", savedOrder.getOrderNumber(), customer.getId());
            return savedOrder;
            
        } catch (Exception e) {
            // Compensating transaction: restore all reserved inventory
            log.error("Error creating order, rolling back inventory reservations: {}", e.getMessage());
            for (OrderItem item : processedItems) {
                inventoryServiceClient.restoreStock(item.getProductId(), item.getQuantity());
            }
            throw e;
        }
    }

    @Transactional
    public Order updateOrderStatus(Long id, Order.OrderStatus newStatus) {
        log.debug("Updating order {} status to {}", id, newStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        // If order is cancelled, restore inventory via REST call
        if (newStatus == Order.OrderStatus.CANCELLED && oldStatus != Order.OrderStatus.CANCELLED) {
            log.info("Cancelling order {}, restoring inventory", order.getOrderNumber());
            for (OrderItem item : order.getItems()) {
                inventoryServiceClient.restoreStock(item.getProductId(), item.getQuantity());
            }
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Updated order {} status from {} to {}", 
                savedOrder.getOrderNumber(), oldStatus, newStatus);
        
        return savedOrder;
    }

    @Transactional
    public void deleteOrder(Long id) {
        log.debug("Deleting order with id: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        // Restore inventory if order was not cancelled via REST call
        if (order.getStatus() != Order.OrderStatus.CANCELLED) {
            log.info("Restoring inventory for deleted order {}", order.getOrderNumber());
            for (OrderItem item : order.getItems()) {
                inventoryServiceClient.restoreStock(item.getProductId(), item.getQuantity());
            }
        }

        orderRepository.deleteById(id);
        log.info("Deleted order {}", order.getOrderNumber());
    }
}
