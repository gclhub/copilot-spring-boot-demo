package com.fisglobal.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisglobal.customer.model.Customer;
import com.fisglobal.customer.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@DisplayName("Customer Controller Tests")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    @DisplayName("Should return all customers")
    void shouldReturnAllCustomers() throws Exception {
        // Arrange
        Customer customer1 = createTestCustomer(1L, "John", "Doe", "john.doe@example.com");
        Customer customer2 = createTestCustomer(2L, "Jane", "Smith", "jane.smith@example.com");
        List<Customer> customers = Arrays.asList(customer1, customer2);

        when(customerService.getAllCustomers()).thenReturn(customers);

        // Act & Assert
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[0].lastName", is("Doe")))
                .andExpect(jsonPath("$[0].email", is("john.doe@example.com")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].firstName", is("Jane")));

        verify(customerService, times(1)).getAllCustomers();
    }

    @Test
    @DisplayName("Should return customer when valid ID")
    void shouldReturnCustomerWhenValidId() throws Exception {
        // Arrange
        Customer customer = createTestCustomer(1L, "John", "Doe", "john.doe@example.com");
        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(customer));

        // Act & Assert
        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.phone", is("555-1234")))
                .andExpect(jsonPath("$.city", is("New York")));

        verify(customerService, times(1)).getCustomerById(1L);
    }

    @Test
    @DisplayName("Should return 404 when invalid ID")
    void shouldReturn404WhenInvalidId() throws Exception {
        // Arrange
        when(customerService.getCustomerById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound());

        verify(customerService, times(1)).getCustomerById(999L);
    }

    @Test
    @DisplayName("Should create customer when valid data")
    void shouldCreateCustomerWhenValidData() throws Exception {
        // Arrange
        Customer customerToCreate = createTestCustomer(null, "Alice", "Johnson", "alice.johnson@example.com");
        Customer createdCustomer = createTestCustomer(3L, "Alice", "Johnson", "alice.johnson@example.com");

        when(customerService.createCustomer(any(Customer.class))).thenReturn(createdCustomer);

        // Act & Assert
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerToCreate)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.firstName", is("Alice")))
                .andExpect(jsonPath("$.lastName", is("Johnson")))
                .andExpect(jsonPath("$.email", is("alice.johnson@example.com")));

        verify(customerService, times(1)).createCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("Should update customer when valid data")
    void shouldUpdateCustomerWhenValidData() throws Exception {
        // Arrange
        Customer updatedDetails = createTestCustomer(null, "John", "Updated", "john.updated@example.com");
        Customer updatedCustomer = createTestCustomer(1L, "John", "Updated", "john.updated@example.com");

        when(customerService.updateCustomer(eq(1L), any(Customer.class))).thenReturn(updatedCustomer);

        // Act & Assert
        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Updated")))
                .andExpect(jsonPath("$.email", is("john.updated@example.com")));

        verify(customerService, times(1)).updateCustomer(eq(1L), any(Customer.class));
    }

    @Test
    @DisplayName("Should delete customer when valid ID")
    void shouldDeleteCustomerWhenValidId() throws Exception {
        // Arrange
        doNothing().when(customerService).deleteCustomer(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).deleteCustomer(1L);
    }

    private Customer createTestCustomer(Long id, String firstName, String lastName, String email) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhone("555-1234");
        customer.setAddress("123 Main St");
        customer.setCity("New York");
        customer.setState("NY");
        customer.setZipCode("10001");
        customer.setCountry("USA");
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return customer;
    }
}
