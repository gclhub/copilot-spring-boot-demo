package com.fisglobal.demo.customer.service;

import com.fisglobal.demo.customer.model.Customer;
import com.fisglobal.demo.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhone("555-1234");
    }

    @Test
    void getAllCustomers_returnsAll() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).hasSize(1).containsExactly(customer);
    }

    @Test
    void getCustomerById_found() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.getCustomerById(1L);

        assertThat(result).isPresent().contains(customer);
    }

    @Test
    void getCustomerById_notFound_returnsEmpty() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.getCustomerById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void getCustomerByEmail_found() {
        when(customerRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.getCustomerByEmail("john.doe@example.com");

        assertThat(result).isPresent().contains(customer);
    }

    @Test
    void getCustomerByEmail_notFound_returnsEmpty() {
        when(customerRepository.findByEmail("nobody@example.com"))
                .thenReturn(Optional.empty());

        Optional<Customer> result = customerService.getCustomerByEmail("nobody@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void createCustomer_success() {
        when(customerRepository.existsByEmail(customer.getEmail())).thenReturn(false);
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer result = customerService.createCustomer(customer);

        assertThat(result).isEqualTo(customer);
        verify(customerRepository).save(customer);
    }

    @Test
    void createCustomer_duplicateEmail_throwsException() {
        when(customerRepository.existsByEmail(customer.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(customer))
                .isInstanceOf(IllegalArgumentException.class);

        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateCustomer_success() {
        Customer update = new Customer();
        update.setFirstName("Jane");
        update.setLastName("Smith");
        update.setEmail("jane.smith@example.com");
        update.setPhone("555-9999");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerService.updateCustomer(1L, update);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    void updateCustomer_notFound_throwsException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomer(99L, customer))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deleteCustomer_success() {
        when(customerRepository.existsById(1L)).thenReturn(true);

        customerService.deleteCustomer(1L);

        verify(customerRepository).deleteById(1L);
    }

    @Test
    void deleteCustomer_notFound_throwsException() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> customerService.deleteCustomer(99L))
                .isInstanceOf(IllegalArgumentException.class);

        verify(customerRepository, never()).deleteById(anyLong());
    }
}
