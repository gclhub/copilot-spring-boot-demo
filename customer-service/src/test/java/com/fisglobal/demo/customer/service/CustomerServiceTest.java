package com.fisglobal.demo.customer.service;

import com.fisglobal.demo.customer.model.Customer;
import com.fisglobal.demo.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerService.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("john.doe@example.com");
        testCustomer.setPhone("555-1234");
        testCustomer.setAddress("123 Main St");
        testCustomer.setCity("New York");
        testCustomer.setState("NY");
        testCustomer.setZipCode("10001");
        testCustomer.setCountry("USA");
    }

    @Test
    void getAllCustomers_ShouldReturnAllCustomers() {
        // Given
        List<Customer> customers = Arrays.asList(testCustomer);
        when(customerRepository.findAll()).thenReturn(customers);

        // When
        List<Customer> result = customerService.getAllCustomers();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john.doe@example.com");
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void getCustomerById_WhenCustomerExists_ShouldReturnCustomer() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        // When
        Optional<Customer> result = customerService.getCustomerById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void createCustomer_WhenEmailIsUnique_ShouldSaveCustomer() {
        // Given
        when(customerRepository.existsByEmail(testCustomer.getEmail())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        Customer result = customerService.createCustomer(testCustomer);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    void getCustomerById_WhenCustomerDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Customer> result = customerService.getCustomerById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(customerRepository, times(1)).findById(999L);
    }

    @Test
    void getCustomerByEmail_WhenCustomerExists_ShouldReturnCustomer() {
        // Given
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testCustomer));

        // When
        Optional<Customer> result = customerService.getCustomerByEmail("john.doe@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
        verify(customerRepository, times(1)).findByEmail("john.doe@example.com");
    }

    @Test
    void getCustomerByEmail_WhenCustomerDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(customerRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When
        Optional<Customer> result = customerService.getCustomerByEmail("notfound@example.com");

        // Then
        assertThat(result).isEmpty();
        verify(customerRepository, times(1)).findByEmail("notfound@example.com");
    }

    @Test
    void createCustomer_WhenEmailAlreadyExists_ShouldThrowException() {
        // Given
        when(customerRepository.existsByEmail(testCustomer.getEmail())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> customerService.createCustomer(testCustomer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(customerRepository, times(1)).existsByEmail(testCustomer.getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_WhenCustomerExists_ShouldUpdateCustomer() {
        // Given
        Customer updatedDetails = new Customer();
        updatedDetails.setFirstName("Jane");
        updatedDetails.setLastName("Smith");
        updatedDetails.setEmail("jane.smith@example.com");
        updatedDetails.setPhone("555-5678");
        updatedDetails.setAddress("456 Oak Ave");
        updatedDetails.setCity("Los Angeles");
        updatedDetails.setState("CA");
        updatedDetails.setZipCode("90001");
        updatedDetails.setCountry("USA");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        Customer result = customerService.updateCustomer(1L, updatedDetails);

        // Then
        assertThat(result).isNotNull();
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void updateCustomer_WhenCustomerDoesNotExist_ShouldThrowException() {
        // Given
        Customer updatedDetails = new Customer();
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customerService.updateCustomer(999L, updatedDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");

        verify(customerRepository, times(1)).findById(999L);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_WhenCustomerExists_ShouldDeleteCustomer() {
        // Given
        when(customerRepository.existsById(1L)).thenReturn(true);

        // When
        customerService.deleteCustomer(1L);

        // Then
        verify(customerRepository, times(1)).existsById(1L);
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCustomer_WhenCustomerDoesNotExist_ShouldThrowException() {
        // Given
        when(customerRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> customerService.deleteCustomer(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");

        verify(customerRepository, times(1)).existsById(999L);
        verify(customerRepository, never()).deleteById(anyLong());
    }
}
