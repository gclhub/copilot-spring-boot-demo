package com.fisglobal.customer.service;

import com.fisglobal.customer.model.Customer;
import com.fisglobal.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

/**
 * Unit tests for CustomerService.
 * Tests follow AAA (Arrange-Act-Assert) pattern and use JUnit 5 best practices.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Tests")
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

    @Nested
    @DisplayName("Get Customer Tests")
    class GetCustomerTests {

        @Test
        @DisplayName("Should return all customers when getAllCustomers is called")
        void shouldReturnAllCustomersWhenGetAllCustomersIsCalled() {
            // Arrange
            List<Customer> customers = Arrays.asList(testCustomer);
            when(customerRepository.findAll()).thenReturn(customers);

            // Act
            List<Customer> result = customerService.getAllCustomers();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testCustomer.getEmail(), result.get(0).getEmail());
            verify(customerRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return customer when valid ID is provided")
        void shouldReturnCustomerWhenValidIdIsProvided() {
            // Arrange
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

            // Act
            Optional<Customer> result = customerService.getCustomerById(1L);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(testCustomer.getEmail(), result.get().getEmail());
            verify(customerRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should return empty when customer ID does not exist")
        void shouldReturnEmptyWhenCustomerIdDoesNotExist() {
            // Arrange
            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Customer> result = customerService.getCustomerById(999L);

            // Assert
            assertFalse(result.isPresent());
            verify(customerRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Should return customer when valid email is provided")
        void shouldReturnCustomerWhenValidEmailIsProvided() {
            // Arrange
            when(customerRepository.findByEmail("john.doe@example.com"))
                    .thenReturn(Optional.of(testCustomer));

            // Act
            Optional<Customer> result = customerService.getCustomerByEmail("john.doe@example.com");

            // Assert
            assertTrue(result.isPresent());
            assertEquals(testCustomer.getEmail(), result.get().getEmail());
            verify(customerRepository, times(1)).findByEmail("john.doe@example.com");
        }
    }

    @Nested
    @DisplayName("Create Customer Tests")
    class CreateCustomerTests {

        @Test
        @DisplayName("Should create customer when valid data is provided")
        void shouldCreateCustomerWhenValidDataIsProvided() {
            // Arrange
            when(customerRepository.existsByEmail(testCustomer.getEmail())).thenReturn(false);
            when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

            // Act
            Customer result = customerService.createCustomer(testCustomer);

            // Assert
            assertNotNull(result);
            assertEquals(testCustomer.getEmail(), result.getEmail());
            verify(customerRepository, times(1)).existsByEmail(testCustomer.getEmail());
            verify(customerRepository, times(1)).save(testCustomer);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Arrange
            when(customerRepository.existsByEmail(testCustomer.getEmail())).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> customerService.createCustomer(testCustomer)
            );
            assertTrue(exception.getMessage().contains("already exists"));
            verify(customerRepository, times(1)).existsByEmail(testCustomer.getEmail());
            verify(customerRepository, never()).save(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("Update Customer Tests")
    class UpdateCustomerTests {

        @Test
        @DisplayName("Should update customer when valid ID is provided")
        void shouldUpdateCustomerWhenValidIdIsProvided() {
            // Arrange
            Customer updatedCustomer = new Customer();
            updatedCustomer.setFirstName("Jane");
            updatedCustomer.setLastName("Smith");
            updatedCustomer.setEmail("jane.smith@example.com");
            updatedCustomer.setPhone("555-5678");

            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
            when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

            // Act
            Customer result = customerService.updateCustomer(1L, updatedCustomer);

            // Assert
            assertNotNull(result);
            verify(customerRepository, times(1)).findById(1L);
            verify(customerRepository, times(1)).save(testCustomer);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent customer")
        void shouldThrowExceptionWhenUpdatingNonExistentCustomer() {
            // Arrange
            when(customerRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> customerService.updateCustomer(999L, testCustomer)
            );
            verify(customerRepository, times(1)).findById(999L);
            verify(customerRepository, never()).save(any(Customer.class));
        }
    }

    @Nested
    @DisplayName("Delete Customer Tests")
    class DeleteCustomerTests {

        @Test
        @DisplayName("Should delete customer when valid ID is provided")
        void shouldDeleteCustomerWhenValidIdIsProvided() {
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
        @DisplayName("Should throw exception when deleting non-existent customer")
        void shouldThrowExceptionWhenDeletingNonExistentCustomer() {
            // Arrange
            when(customerRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> customerService.deleteCustomer(999L)
            );
            verify(customerRepository, times(1)).existsById(999L);
            verify(customerRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should return true when customer exists")
        void shouldReturnTrueWhenCustomerExists() {
            // Arrange
            when(customerRepository.existsById(1L)).thenReturn(true);

            // Act
            boolean result = customerService.existsById(1L);

            // Assert
            assertTrue(result);
            verify(customerRepository, times(1)).existsById(1L);
        }

        @Test
        @DisplayName("Should return false when customer does not exist")
        void shouldReturnFalseWhenCustomerDoesNotExist() {
            // Arrange
            when(customerRepository.existsById(999L)).thenReturn(false);

            // Act
            boolean result = customerService.existsById(999L);

            // Assert
            assertFalse(result);
            verify(customerRepository, times(1)).existsById(999L);
        }
    }
}
