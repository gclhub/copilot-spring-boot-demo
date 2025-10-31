package com.fisglobal.demo.customer.config;

import com.fisglobal.demo.customer.model.Customer;
import com.fisglobal.demo.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Data initialization configuration for Customer Service.
 * Loads sample customer data into the database on application startup.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class CustomerDataInitializer {

    @Bean
    CommandLineRunner initCustomerDatabase(CustomerRepository customerRepository) {
        return args -> {
            log.info("Initializing customer database with sample data...");

            // Create sample customers
            if (customerRepository.count() == 0) {
                log.info("Creating sample customers...");
                
                Customer customer1 = new Customer();
                customer1.setFirstName("John");
                customer1.setLastName("Doe");
                customer1.setEmail("john.doe@example.com");
                customer1.setPhone("555-1234");
                customer1.setAddress("123 Main St");
                customer1.setCity("New York");
                customer1.setState("NY");
                customer1.setZipCode("10001");
                customer1.setCountry("USA");
                customerRepository.save(customer1);

                Customer customer2 = new Customer();
                customer2.setFirstName("Jane");
                customer2.setLastName("Smith");
                customer2.setEmail("jane.smith@example.com");
                customer2.setPhone("555-5678");
                customer2.setAddress("456 Oak Ave");
                customer2.setCity("Los Angeles");
                customer2.setState("CA");
                customer2.setZipCode("90001");
                customer2.setCountry("USA");
                customerRepository.save(customer2);

                Customer customer3 = new Customer();
                customer3.setFirstName("Bob");
                customer3.setLastName("Johnson");
                customer3.setEmail("bob.johnson@example.com");
                customer3.setPhone("555-9012");
                customer3.setAddress("789 Pine Rd");
                customer3.setCity("Chicago");
                customer3.setState("IL");
                customer3.setZipCode("60601");
                customer3.setCountry("USA");
                customerRepository.save(customer3);

                log.info("Created {} customers", customerRepository.count());
            }

            log.info("Customer database initialization complete!");
        };
    }
}
