package com.fisglobal.demo.order.client;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
