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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        testCustomer.setPhone("123-456-7890");
        testCustomer.setAddress("123 Main St");
        testCustomer.setCity("Springfield");
        testCustomer.setState("IL");
        testCustomer.setZipCode("62701");
        testCustomer.setCountry("USA");
    }

    @Test
    void getAllCustomers_ShouldReturnAllCustomers() {
        // Arrange
        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setEmail("jane.smith@example.com");
        List<Customer> customers = Arrays.asList(testCustomer, customer2);
        when(customerRepository.findAll()).thenReturn(customers);

        // Act
        List<Customer> result = customerService.getAllCustomers();

        // Assert
        assertEquals(2, result.size());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void getCustomerById_WhenCustomerExists_ShouldReturnCustomer() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        // Act
        Optional<Customer> result = customerService.getCustomerById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void getCustomerById_WhenCustomerDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Customer> result = customerService.getCustomerById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(customerRepository, times(1)).findById(999L);
    }

    @Test
    void getCustomerByEmail_WhenCustomerExists_ShouldReturnCustomer() {
        // Arrange
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testCustomer));

        // Act
        Optional<Customer> result = customerService.getCustomerByEmail("john.doe@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("john.doe@example.com", result.get().getEmail());
        verify(customerRepository, times(1)).findByEmail("john.doe@example.com");
    }

    @Test
    void getCustomerByEmail_WhenCustomerDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(customerRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<Customer> result = customerService.getCustomerByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
        verify(customerRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void createCustomer_WhenEmailDoesNotExist_ShouldCreateCustomer() {
        // Arrange
        when(customerRepository.existsByEmail(testCustomer.getEmail())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // Act
        Customer result = customerService.createCustomer(testCustomer);

        // Assert
        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        verify(customerRepository, times(1)).existsByEmail(testCustomer.getEmail());
        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    void createCustomer_WhenEmailExists_ShouldThrowException() {
        // Arrange
        when(customerRepository.existsByEmail(testCustomer.getEmail())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.createCustomer(testCustomer);
        });
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(customerRepository, times(1)).existsByEmail(testCustomer.getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_WhenCustomerExists_ShouldUpdateCustomer() {
        // Arrange
        Customer updatedDetails = new Customer();
        updatedDetails.setFirstName("Jane");
        updatedDetails.setLastName("Smith");
        updatedDetails.setEmail("jane.smith@example.com");
        updatedDetails.setPhone("987-654-3210");
        updatedDetails.setAddress("456 Oak Ave");
        updatedDetails.setCity("Chicago");
        updatedDetails.setState("IL");
        updatedDetails.setZipCode("60601");
        updatedDetails.setCountry("USA");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // Act
        Customer result = customerService.updateCustomer(1L, updatedDetails);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("jane.smith@example.com", result.getEmail());
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    void updateCustomer_WhenCustomerDoesNotExist_ShouldThrowException() {
        // Arrange
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.updateCustomer(999L, testCustomer);
        });
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(customerRepository, times(1)).findById(999L);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_WhenCustomerExists_ShouldDeleteCustomer() {
        // Arrange
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        // Act
        customerService.deleteCustomer(1L);

        // Assert
        verify(customerRepository, times(1)).existsById(1L);
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCustomer_WhenCustomerDoesNotExist_ShouldThrowException() {
        // Arrange
        when(customerRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerService.deleteCustomer(999L);
        });
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(customerRepository, times(1)).existsById(999L);
        verify(customerRepository, never()).deleteById(anyLong());
    }
}
