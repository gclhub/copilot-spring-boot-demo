package com.fisglobal.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Configuration properties for external service URLs.
 * All service URLs are externalized to application.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "")
@Validated
@Data
public class ServiceConfig {

    @NotBlank
    private String customerServiceUrl;

    @NotBlank
    private String inventoryServiceUrl;
}
