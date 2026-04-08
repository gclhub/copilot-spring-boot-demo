package com.fisglobal.demo.order.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Customer information returned from Customer Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
