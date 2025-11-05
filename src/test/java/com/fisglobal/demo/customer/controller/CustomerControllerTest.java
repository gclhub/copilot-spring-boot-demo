package com.fisglobal.demo.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisglobal.demo.customer.model.Customer;
import com.fisglobal.demo.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void getAllCustomers_ShouldReturnCustomerList() throws Exception {
        // Arrange
        when(customerService.getAllCustomers()).thenReturn(Arrays.asList(testCustomer));

        // Act & Assert
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));

        verify(customerService, times(1)).getAllCustomers();
    }

    @Test
    void getCustomerById_WhenExists_ShouldReturnCustomer() throws Exception {
        // Arrange
        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(testCustomer));

        // Act & Assert
        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(customerService, times(1)).getCustomerById(1L);
    }

    @Test
    void getCustomerById_WhenNotExists_ShouldReturn404() throws Exception {
        // Arrange
        when(customerService.getCustomerById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound());

        verify(customerService, times(1)).getCustomerById(999L);
    }

    @Test
    void getCustomerByEmail_WhenExists_ShouldReturnCustomer() throws Exception {
        // Arrange
        when(customerService.getCustomerByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testCustomer));

        // Act & Assert
        mockMvc.perform(get("/api/customers/email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(customerService, times(1)).getCustomerByEmail("john.doe@example.com");
    }

    @Test
    void getCustomerByEmail_WhenNotExists_ShouldReturn404() throws Exception {
        // Arrange
        when(customerService.getCustomerByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/customers/email/nonexistent@example.com"))
                .andExpect(status().isNotFound());

        verify(customerService, times(1)).getCustomerByEmail("nonexistent@example.com");
    }

    @Test
    void createCustomer_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        when(customerService.createCustomer(any(Customer.class))).thenReturn(testCustomer);

        // Act & Assert
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCustomer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(customerService, times(1)).createCustomer(any(Customer.class));
    }

    @Test
    void updateCustomer_WhenExists_ShouldReturnUpdatedCustomer() throws Exception {
        // Arrange
        Customer updatedCustomer = new Customer();
        updatedCustomer.setFirstName("Jane");
        updatedCustomer.setLastName("Smith");
        updatedCustomer.setEmail("jane.smith@example.com");
        updatedCustomer.setPhone("987-654-3210");

        when(customerService.updateCustomer(eq(1L), any(Customer.class))).thenReturn(updatedCustomer);

        // Act & Assert
        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));

        verify(customerService, times(1)).updateCustomer(eq(1L), any(Customer.class));
    }

    @Test
    void updateCustomer_WhenNotExists_ShouldReturn404() throws Exception {
        // Arrange
        when(customerService.updateCustomer(eq(999L), any(Customer.class)))
                .thenThrow(new IllegalArgumentException("Customer not found"));

        // Act & Assert
        mockMvc.perform(put("/api/customers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCustomer)))
                .andExpect(status().isNotFound());

        verify(customerService, times(1)).updateCustomer(eq(999L), any(Customer.class));
    }

    @Test
    void deleteCustomer_WhenExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(customerService).deleteCustomer(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).deleteCustomer(1L);
    }

    @Test
    void deleteCustomer_WhenNotExists_ShouldReturn404() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Customer not found"))
                .when(customerService).deleteCustomer(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/customers/999"))
                .andExpect(status().isNotFound());

        verify(customerService, times(1)).deleteCustomer(999L);
    }
}
