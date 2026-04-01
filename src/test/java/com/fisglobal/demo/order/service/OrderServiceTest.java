package com.fisglobal.demo.order.service;

import com.fisglobal.demo.customer.model.Customer;
import com.fisglobal.demo.customer.service.CustomerService;
import com.fisglobal.demo.inventory.model.Product;
import com.fisglobal.demo.inventory.service.ProductService;
import com.fisglobal.demo.order.dto.CreateOrderRequest;
import com.fisglobal.demo.order.dto.OrderItemRequest;
import com.fisglobal.demo.order.model.Order;
import com.fisglobal.demo.order.model.OrderItem;
import com.fisglobal.demo.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Product product;
    private Order order;
    private CreateOrderRequest createRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");

        product = new Product();
        product.setId(10L);
        product.setName("Widget");
        product.setSku("WIDGET-001");
        product.setPrice(new BigDecimal("9.99"));
        product.setStockQuantity(50);
        product.setActive(true);

        order = new Order();
        order.setId(100L);
        order.setCustomerId(1L);
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setItems(new ArrayList<>());

        OrderItemRequest itemRequest = new OrderItemRequest(10L, 2);
        createRequest = new CreateOrderRequest(1L, List.of(itemRequest),
                "123 Main St", "Springfield", "IL", "62701", "US");
    }

    // ---- Read operations ----

    @Test
    void getAllOrders_returnsAll() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> result = orderService.getAllOrders();

        assertThat(result).hasSize(1).containsExactly(order);
    }

    @Test
    void getOrderById_found() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderById(100L);

        assertThat(result).isPresent().contains(order);
    }

    @Test
    void getOrderById_notFound_returnsEmpty() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.getOrderById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void getOrderByOrderNumber_found() {
        when(orderRepository.findByOrderNumber("ORD-123")).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderByOrderNumber("ORD-123");

        assertThat(result).isPresent().contains(order);
    }

    @Test
    void getOrdersByCustomerId_returnsList() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(List.of(order));

        List<Order> result = orderService.getOrdersByCustomerId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getOrdersByStatus_returnsList() {
        when(orderRepository.findByStatus(Order.OrderStatus.CONFIRMED)).thenReturn(List.of(order));

        List<Order> result = orderService.getOrdersByStatus(Order.OrderStatus.CONFIRMED);

        assertThat(result).hasSize(1).containsExactly(order);
    }

    // ---- createOrder ----

    @Test
    void createOrder_success() {
        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(customer));
        when(productService.getProductById(10L)).thenReturn(Optional.of(product));
        when(productService.reserveStock(10L, 2)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(1L);
        verify(productService).reserveStock(10L, 2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_customerNotFound_throwsException() {
        when(customerService.getCustomerById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer not found");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_productNotFound_throwsException() {
        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(customer));
        when(productService.getProductById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_productOutOfStock_throwsException() {
        product.setStockQuantity(0);
        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(customer));
        when(productService.getProductById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("out of stock");

        verify(productService, never()).reserveStock(anyLong(), anyInt());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_insufficientStock_throwsException() {
        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(customer));
        when(productService.getProductById(10L)).thenReturn(Optional.of(product));
        when(productService.reserveStock(10L, 2)).thenReturn(false);

        assertThatThrownBy(() -> orderService.createOrder(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any());
    }

    // ---- updateOrderStatus ----

    @Test
    void updateOrderStatus_toConfirmed_noStockRestore() {
        order.setStatus(Order.OrderStatus.PENDING);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateOrderStatus(100L, Order.OrderStatus.CONFIRMED);

        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
        verify(productService, never()).restoreStock(anyLong(), anyInt());
    }

    @Test
    void updateOrderStatus_toCancelled_restoresStock() {
        OrderItem item = new OrderItem();
        item.setProductId(10L);
        item.setQuantity(2);
        order.getItems().add(item);
        order.setStatus(Order.OrderStatus.CONFIRMED);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateOrderStatus(100L, Order.OrderStatus.CANCELLED);

        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
        verify(productService).restoreStock(10L, 2);
    }

    @Test
    void updateOrderStatus_alreadyCancelled_noDoubleRestore() {
        order.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.updateOrderStatus(100L, Order.OrderStatus.CANCELLED);

        verify(productService, never()).restoreStock(anyLong(), anyInt());
    }

    @Test
    void updateOrderStatus_orderNotFound_throwsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(999L, Order.OrderStatus.CANCELLED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");
    }

    // ---- deleteOrder ----

    @Test
    void deleteOrder_notCancelled_restoresStock() {
        OrderItem item = new OrderItem();
        item.setProductId(10L);
        item.setQuantity(3);
        order.getItems().add(item);
        order.setStatus(Order.OrderStatus.CONFIRMED);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(100L);

        verify(productService).restoreStock(10L, 3);
        verify(orderRepository).deleteById(100L);
    }

    @Test
    void deleteOrder_alreadyCancelled_noStockRestore() {
        order.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(100L);

        verify(productService, never()).restoreStock(anyLong(), anyInt());
        verify(orderRepository).deleteById(100L);
    }

    @Test
    void deleteOrder_notFound_throwsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.deleteOrder(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");

        verify(orderRepository, never()).deleteById(anyLong());
    }
}
