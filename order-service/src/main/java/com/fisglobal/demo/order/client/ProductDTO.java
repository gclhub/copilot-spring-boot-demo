package com.fisglobal.demo.order.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Product information returned from Inventory Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private String category;
    private Integer reorderLevel;
    private Boolean active;
}
