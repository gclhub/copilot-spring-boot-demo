package com.fisglobal.demo.order.service;

import com.fisglobal.demo.order.client.CustomerClient;
import com.fisglobal.demo.order.client.CustomerDTO;
import com.fisglobal.demo.order.client.InventoryClient;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for Order operations in a microservices architecture.
 * Communicates with Customer and Inventory services via REST APIs.
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
    public List<Order> getOrdersByCustomerId(Long customerId) {
        log.debug("Fetching orders for customer: {}", customerId);
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrderByOrderNumber(String orderNumber) {
        log.debug("Fetching order with order number: {}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        log.debug("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        // Step 1: Validate customer exists (call to Customer Service)
        CustomerDTO customer;
        try {
            customer = customerClient.getCustomerById(request.getCustomerId());
            if (customer == null) {
                log.error("Customer not found: {}", request.getCustomerId());
                throw new RuntimeException("Customer not found: " + request.getCustomerId());
            }
            log.debug("Customer validated: {}", customer.getEmail());
        } catch (Exception e) {
            log.error("Failed to validate customer: {}", e.getMessage());
            throw new RuntimeException("Failed to validate customer", e);
        }

        // Step 2: Create order entity
        Order order = new Order();
        order.setCustomerId(customer.getId());
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingZip(request.getShippingZip());
        order.setShippingCountry(request.getShippingCountry());

        // Step 3: Process order items (call to Inventory Service)
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            try {
                // Fetch product from Inventory Service
                ProductDTO product = inventoryClient.getProductById(itemRequest.getProductId());
                if (product == null) {
                    log.error("Product not found: {}", itemRequest.getProductId());
                    throw new RuntimeException("Product not found: " + itemRequest.getProductId());
                }

                log.debug("Processing order item for product: {}", product.getName());

                // Reserve stock in Inventory Service
                try {
                    inventoryClient.reserveStock(product.getId(), itemRequest.getQuantity());
                    log.debug("Stock reserved for product: {}", product.getName());
                } catch (Exception e) {
                    log.error("Failed to reserve stock for product {}: {}", product.getName(), e.getMessage());
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }

                // Create order item
                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(product.getId());
                orderItem.setProductName(product.getName());
                orderItem.setProductSku(product.getSku());
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setUnitPrice(product.getPrice());
                orderItem.setSubtotal(product.getPrice().multiply(new BigDecimal(itemRequest.getQuantity())));
                orderItems.add(orderItem);

                totalAmount = totalAmount.add(orderItem.getSubtotal());
            } catch (Exception e) {
                log.error("Failed to process order item: {}", e.getMessage());
                // Rollback: restore stock for items processed so far
                rollbackStockReservations(orderItems);
                throw e;
            }
        }

        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {} for customer: {}", savedOrder.getOrderNumber(), customer.getId());

        return savedOrder;
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING && 
            order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new RuntimeException("Order cannot be cancelled in status: " + order.getStatus());
        }

        // Restore stock in Inventory Service
        log.debug("Restoring stock for cancelled order: {}", order.getOrderNumber());
        for (OrderItem item : order.getItems()) {
            try {
                inventoryClient.restoreStock(item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to restore stock for product {}: {}", item.getProductId(), e.getMessage());
            }
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        log.info("Order cancelled: {}", order.getOrderNumber());

        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        log.info("Updating order {} status to {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Validate status transition
        Order.OrderStatus currentStatus = order.getStatus();
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            log.error("Invalid status transition from {} to {}", currentStatus, newStatus);
            throw new RuntimeException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        // If cancelling, restore stock
        if (newStatus == Order.OrderStatus.CANCELLED) {
            log.debug("Restoring stock for cancelled order: {}", order.getOrderNumber());
            for (OrderItem item : order.getItems()) {
                try {
                    inventoryClient.restoreStock(item.getProductId(), item.getQuantity());
                } catch (Exception e) {
                    log.error("Failed to restore stock for product {}: {}", item.getProductId(), e.getMessage());
                }
            }
        }

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        log.info("Order status updated: {} -> {}", order.getOrderNumber(), newStatus);

        return savedOrder;
    }

    @Transactional
    public void deleteOrder(Long id) {
        log.info("Deleting order: {}", id);
        orderRepository.deleteById(id);
    }

    private void rollbackStockReservations(List<OrderItem> processedItems) {
        log.warn("Rolling back stock reservations for {} items", processedItems.size());
        for (OrderItem item : processedItems) {
            try {
                inventoryClient.restoreStock(item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to restore stock during rollback for product {}: {}", 
                         item.getProductId(), e.getMessage());
            }
        }
    }

    private boolean isValidStatusTransition(Order.OrderStatus current, Order.OrderStatus next) {
        // Define valid status transitions
        return switch (current) {
            case PENDING -> next == Order.OrderStatus.CONFIRMED || 
                           next == Order.OrderStatus.CANCELLED;
            case CONFIRMED -> next == Order.OrderStatus.PROCESSING || 
                             next == Order.OrderStatus.CANCELLED;
            case PROCESSING -> next == Order.OrderStatus.SHIPPED || 
                              next == Order.OrderStatus.CANCELLED;
            case SHIPPED -> next == Order.OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false; // Terminal states
        };
    }
}
