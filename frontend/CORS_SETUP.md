# CORS Configuration for Microservices

To enable the Angular frontend to communicate with the Spring Boot microservices, CORS (Cross-Origin Resource Sharing) must be configured on each backend service.

## Why CORS is Needed

The Angular application runs on `http://localhost:4200` (development server), while the microservices run on different ports:
- Customer Service: `http://localhost:8081`
- Inventory Service: `http://localhost:8082`
- Order Service: `http://localhost:8083`

Browsers enforce the Same-Origin Policy, which blocks requests from the frontend to the backend unless CORS is properly configured.

## CORS Configuration

Add the following `CorsConfig.java` file to each microservice:

### Customer Service
Create: `customer-service/src/main/java/com/fisglobal/demo/customer/config/CorsConfig.java`

```java
package com.fisglobal.demo.customer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        corsConfiguration.setAllowedMethods(Arrays.asList("*"));
        corsConfiguration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        
        return new CorsFilter(source);
    }
}
```

### Inventory Service
Create: `inventory-service/src/main/java/com/fisglobal/demo/inventory/config/CorsConfig.java`

```java
package com.fisglobal.demo.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        corsConfiguration.setAllowedMethods(Arrays.asList("*"));
        corsConfiguration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        
        return new CorsFilter(source);
    }
}
```

### Order Service
Create: `order-service/src/main/java/com/fisglobal/demo/order/config/CorsConfig.java`

```java
package com.fisglobal.demo.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        corsConfiguration.setAllowedMethods(Arrays.asList("*"));
        corsConfiguration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        
        return new CorsFilter(source);
    }
}
```

## Applying the Configuration

After adding the CORS configuration files:

1. Rebuild each microservice:
   ```bash
   cd customer-service && mvn clean compile
   cd ../inventory-service && mvn clean compile
   cd ../order-service && mvn clean compile
   ```

2. Restart all microservices for the changes to take effect.

3. The Angular frontend should now be able to communicate with all backend services without CORS errors.

## Verification

Test that CORS is working:

```bash
# Test Customer Service CORS
curl -v -H "Origin: http://localhost:4200" \
  -H "Access-Control-Request-Method: GET" \
  -X OPTIONS http://localhost:8081/api/customers

# Test Inventory Service CORS
curl -v -H "Origin: http://localhost:4200" \
  -H "Access-Control-Request-Method: GET" \
  -X OPTIONS http://localhost:8082/api/products

# Test Order Service CORS
curl -v -H "Origin: http://localhost:4200" \
  -H "Access-Control-Request-Method: GET" \
  -X OPTIONS http://localhost:8083/api/orders
```

You should see `Access-Control-Allow-Origin: http://localhost:4200` in the response headers.

## Production Considerations

For production deployments:

1. Replace `http://localhost:4200` with your actual frontend URL
2. Consider using environment variables for allowed origins
3. Restrict allowed methods and headers as needed
4. Add proper security headers
5. Consider using an API Gateway to handle CORS centrally
