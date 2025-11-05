package com.fisglobal.demo.customer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhone("123-456-7890");
        customer.setAddress("123 Main St");
        customer.setCity("Springfield");
        customer.setState("IL");
        customer.setZipCode("62701");
        customer.setCountry("USA");
    }

    @Test
    void onCreate_ShouldSetCreatedAtAndUpdatedAt() {
        // Arrange
        Customer newCustomer = new Customer();

        // Act
        newCustomer.onCreate();

        // Assert
        assertNotNull(newCustomer.getCreatedAt());
        assertNotNull(newCustomer.getUpdatedAt());
    }

    @Test
    void onUpdate_ShouldUpdateTimestamp() {
        // Arrange
        customer.onCreate();

        // Act
        customer.onUpdate();

        // Assert
        assertNotNull(customer.getUpdatedAt());
    }

    @Test
    void customerConstructor_WithAllArgs_ShouldSetAllFields() {
        // Act
        Customer fullCustomer = new Customer(
            1L, 
            "Jane", 
            "Smith", 
            "jane.smith@example.com",
            "987-654-3210",
            "456 Oak Ave",
            "Chicago",
            "IL",
            "60601",
            "USA",
            null,
            null
        );

        // Assert
        assertEquals(1L, fullCustomer.getId());
        assertEquals("Jane", fullCustomer.getFirstName());
        assertEquals("Smith", fullCustomer.getLastName());
        assertEquals("jane.smith@example.com", fullCustomer.getEmail());
        assertEquals("987-654-3210", fullCustomer.getPhone());
        assertEquals("456 Oak Ave", fullCustomer.getAddress());
        assertEquals("Chicago", fullCustomer.getCity());
        assertEquals("IL", fullCustomer.getState());
        assertEquals("60601", fullCustomer.getZipCode());
        assertEquals("USA", fullCustomer.getCountry());
    }

    @Test
    void customerSettersAndGetters_ShouldWorkCorrectly() {
        // Act & Assert
        assertEquals(1L, customer.getId());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals("john.doe@example.com", customer.getEmail());
        assertEquals("123-456-7890", customer.getPhone());
        assertEquals("123 Main St", customer.getAddress());
        assertEquals("Springfield", customer.getCity());
        assertEquals("IL", customer.getState());
        assertEquals("62701", customer.getZipCode());
        assertEquals("USA", customer.getCountry());
    }
}
