# Copilot Instructions for E-Commerce Microservices Demo

## Project Context

This is a **Spring Boot monolith-to-microservices demo project** for teaching microservices refactoring with GitHub Copilot. The codebase exists in two states:
- **`main` branch**: Monolithic e-commerce app (Customer/Inventory/Order domains in one app on port 8080)
- **`completed-demo` branch**: Three independent microservices (ports 8081-8083) with database-per-service

The monolith is intentionally designed to show tight coupling that will be split during live demos.

## Critical Build Requirements

### Java 17 Enforcement (MANDATORY)
- **Always use Java 17**, NOT Java 21 - Lombok has TypeTag::UNKNOWN errors on Java 21
- Build scripts (`build.sh`, `run.sh`) enforce Java 17 via `JAVA_HOME=$(/usr/libexec/java_home -v 17)`
- Maven compiler plugin configured for Java 17 with Lombok annotation processing (version 3.13.0)
- When creating new services, copy this pattern from existing `pom.xml` files

### Build Commands
```bash
# Monolith (from root)
./build.sh  # Forces Java 17
./run.sh    # Starts on port 8080

# Individual microservices (from service directory)
./run.sh    # Each service has its own run script
```

## Architecture Patterns to Preserve

### Monolith Structure (main branch)
```
src/main/java/com/fisglobal/demo/
├── EcommerceApplication.java         # Single entry point
├── config/DataInitializer.java       # Loads 3 customers + 6 products
├── customer/                         # Domain: Customer management
├── inventory/                        # Domain: Product/stock management  
└── order/                            # Domain: Order processing (TIGHTLY COUPLED)
    └── service/OrderService.java     # Directly injects CustomerService + ProductService
```

**Key coupling point**: `OrderService` uses constructor injection of `CustomerService` and `ProductService` - this is the primary refactoring target.

### Microservices Structure (completed-demo branch)
```
customer-service/  (port 8081, customerdb)
inventory-service/ (port 8082, inventorydb)
order-service/     (port 8083, orderdb)
├── client/CustomerClient.java   # RestTemplate wrapper for http://localhost:8081
└── client/InventoryClient.java  # RestTemplate wrapper for http://localhost:8082
```

**Inter-service communication**: Order service calls Customer and Inventory via `RestTemplate` (not WebClient/RestClient). Stock reservation uses POST to `/api/products/{id}/reserve?quantity=X`.

## Demo Workflow Conventions

### Creating New Microservices
When extracting a domain into a microservice, follow this exact pattern:

1. **Copy domain files** from monolith maintaining package structure:
   ```bash
   cp -a ../src/main/java/com/fisglobal/demo/customer/model/Customer.java \
         src/main/java/com/fisglobal/customer/model/
   ```

2. **Update package names**: `com.fisglobal.demo.customer` → `com.fisglobal.customer`

3. **Create application class**:
   ```java
   package com.fisglobal.customer;
   @SpringBootApplication
   public class CustomerServiceApplication {
       public static void main(String[] args) {
           SpringApplication.run(CustomerServiceApplication.class, args);
       }
   }
   ```

4. **application.properties template**:
   ```properties
   spring.application.name=customer-service
   server.port=8081
   spring.datasource.url=jdbc:h2:mem:customerdb
   spring.datasource.username=sa
   spring.datasource.password=
   spring.jpa.hibernate.ddl-auto=create-drop
   spring.jpa.show-sql=true
   spring.h2.console.enabled=true
   logging.level.com.fisglobal.customer=DEBUG
   ```

5. **Add DataInitializer** to populate sample data on startup (see `customer-service/src/main/java/com/fisglobal/customer/config/DataInitializer.java`)

6. **Create run.sh** to enforce Java 17 (copy from existing services)

### Port and Database Allocations
| Service | Port | Database Name | Description |
|---------|------|---------------|-------------|
| Monolith | 8080 | ecommercedb | Starting point |
| Customer | 8081 | customerdb | 3 sample customers |
| Inventory | 8082 | inventorydb | 6 sample products |
| Order | 8083 | orderdb | Calls 8081 + 8082 |

**Never reuse databases across services** - this violates microservices boundaries.

## Configuration Management

### Externalization (Critical Rule from Custom Instructions)
- **NEVER hardcode** service URLs, ports, or database names in Java code
- Use `application.properties` with environment variable fallbacks:
  ```properties
  customer.service.url=${CUSTOMER_SERVICE_URL:http://localhost:8081}
  inventory.service.url=${INVENTORY_SERVICE_URL:http://localhost:8082}
  ```

### RestTemplate Configuration
Order service uses `RestTemplate` bean for HTTP calls:
```java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

## Testing and Data Patterns

### Sample Data Initialization
All services use `@Configuration` + `CommandLineRunner` pattern to load data:
- Check `repository.count() == 0` before inserting
- Use Lombok `@RequiredArgsConstructor` for dependency injection
- Log initialization with SLF4J: `log.info("Created {} customers", count)`

### API Testing Pattern
```bash
# Customer service
curl http://localhost:8081/api/customers

# Inventory service  
curl http://localhost:8082/api/products

# Create order (requires both services running)
curl -X POST http://localhost:8083/api/orders -H "Content-Type: application/json" \
  -d '{"customerId":1,"items":[{"productId":1,"quantity":2}],"shippingAddress":"123 Main",...}'
```

## Documentation Files (Keep Updated)

- **docs/DEMO_SCRIPT.md**: 60-minute live demo guide with exact Copilot prompts
- **docs/COPILOT_PROMPTS.md**: 50+ reusable prompts for refactoring tasks
- **docs/BUILD_NOTES.md**: Java 17 setup, troubleshooting, IDE configuration
- **MICROSERVICES_README.md**: Quick reference for microservices setup (on main branch)

When making architectural changes, update these docs to reflect current patterns.

## Common Issues (Prevention)

1. **Java 21 errors**: Always specify Java 17 in `JAVA_HOME` before building
2. **Port conflicts**: Use `lsof -ti:8081 | xargs kill -9` to free ports
3. **Database name collisions**: Each service needs unique database name in `spring.datasource.url`
4. **Missing Lombok processing**: Ensure `maven-compiler-plugin` 3.13.0 with `<annotationProcessorPaths>` for Lombok
5. **Service communication failures**: Verify all dependent services are running before testing Order service

## Code Style (From Original Custom Instructions)

- Use Java 17 features, Lombok `@RequiredArgsConstructor` for constructor injection
- Follow AAA pattern in tests: Arrange-Act-Assert
- Use `@Slf4j` for logging, never log sensitive data
- REST endpoints: plural nouns (`/customers`, `/products`, `/orders`)
- DTOs for requests/responses, never expose entities directly
- HTTP status codes: 200 (OK), 201 (Created), 204 (No Content), 400 (Bad Request), 404 (Not Found)
- JUnit 5 with `@DisplayName` and descriptive test names: `shouldCreateOrderWhenValidCustomerAndStock()`

See original custom instructions below for complete style guide.

---

# Original Custom Instructions (Reference)

## General Coding Standards

## General Coding Standards

### Java Version and Best Practices
- Use Java 17 or later features
- Prefer records for DTOs and immutable data classes
- Use `var` for local variables where type is obvious
- Use modern Java APIs (Stream API, Optional, etc.)
- Follow Oracle Java Code Conventions
- Use meaningful variable and method names that reflect business domain

### Code Style
- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Use braces for all control structures, even single-line statements
- Place opening braces on the same line
- Use blank lines to separate logical sections
- Add JavaDoc comments for all public classes, methods, and interfaces

### Spring Boot Best Practices
- Use constructor injection with `@RequiredArgsConstructor` (Lombok) instead of field injection
- Prefer `@RestController` for REST APIs
- Use `@Service`, `@Repository`, `@Component` appropriately
- Leverage Spring Boot auto-configuration
- Use `@Transactional` appropriately for database operations
- Add `readOnly = true` for read-only transactions

## Configuration Management

### Externalization Requirements
- **NEVER** hardcode configuration values in Java code
- All configuration MUST be externalized to `application.properties` or `application.yml`
- This includes:
  - Port numbers (`server.port`)
  - Service URLs (`customer.service.url`, `inventory.service.url`)
  - Database connection settings
  - Timeouts and retry configurations
  - API keys and credentials
  - Feature flags
  - Thread pool sizes

### Property File Organization
Structure `application.properties` with clear sections:
```properties
# Application Configuration
spring.application.name=service-name
server.port=${SERVICE_PORT:8080}

# Database Configuration
spring.datasource.url=${DB_URL:jdbc:h2:mem:defaultdb}
spring.datasource.username=${DB_USER:sa}

# External Service Configuration
external.service.url=${EXTERNAL_SERVICE_URL:http://localhost:8081}
external.service.timeout=${EXTERNAL_SERVICE_TIMEOUT:5000}

# Logging Configuration
logging.level.com.fisglobal=${LOG_LEVEL:INFO}
```

### Configuration Classes
- Use `@ConfigurationProperties` for type-safe configuration
- Group related properties in dedicated configuration classes
- Validate configuration properties using `@Validated` and Bean Validation annotations

## Testing Standards

### JUnit 5 Requirements
- Use JUnit 5 (Jupiter) for all new tests
- Organize tests using `@Nested` classes for related test scenarios
- Use `@DisplayName` for readable test descriptions
- Use `@ParameterizedTest` for testing multiple scenarios

### Test Structure
Follow the Arrange-Act-Assert (AAA) pattern:
```java
@Test
@DisplayName("Should create customer when valid data is provided")
void shouldCreateCustomerWhenValidData() {
    // Arrange
    CreateCustomerRequest request = new CreateCustomerRequest(...);
    
    // Act
    Customer result = customerService.createCustomer(request);
    
    // Assert
    assertNotNull(result.getId());
    assertEquals(request.getEmail(), result.getEmail());
}
```

### Test Coverage Requirements
Create tests for:
- **Service Layer**: Unit tests with mocked dependencies using `@Mock` and `@InjectMocks`
- **Controller Layer**: Integration tests using `@WebMvcTest` and `MockMvc`
- **Repository Layer**: Tests using `@DataJpaTest` where needed
- **Edge Cases**: Null values, empty collections, boundary conditions
- **Error Scenarios**: Invalid input, constraint violations, service failures

### Mocking and Stubbing
- Use Mockito for mocking dependencies
- Use `@MockBean` in Spring tests to mock beans
- Use `when().thenReturn()` for stubbing method calls
- Use `verify()` to assert method calls
- Mock external service calls in microservices

### Test Naming Convention
Use descriptive method names that describe the test:
- Format: `should[ExpectedBehavior]When[Condition]`
- Examples:
  - `shouldReturnCustomerWhenIdExists()`
  - `shouldThrowExceptionWhenEmailIsDuplicate()`
  - `shouldReserveStockWhenQuantityIsAvailable()`

### Spring Boot Test Annotations
- Use `@SpringBootTest` for full integration tests
- Use `@WebMvcTest` for controller layer tests
- Use `@DataJpaTest` for repository layer tests
- Use `@MockBean` to replace beans with mocks in Spring context
- Use `@TestConfiguration` for test-specific configuration

## Microservices Architecture

### Service Independence
- Each microservice should be independently deployable
- Services should not share databases
- Services should not directly access other services' data stores
- Each service should have its own build and test lifecycle

### Inter-Service Communication
- Use REST APIs for synchronous communication
- Use `RestClient` (Spring 6.1+) or `WebClient` for making HTTP calls
- Define clear API contracts with DTOs
- Version APIs appropriately (e.g., `/api/v1/customers`)
- Implement circuit breakers for resilience (Resilience4j)
- Set appropriate timeouts for all external calls

### Error Handling in Distributed Systems
- Return appropriate HTTP status codes (200, 201, 400, 404, 500, 503)
- Implement global exception handling with `@ControllerAdvice`
- Log errors with sufficient context for debugging
- Handle partial failures gracefully
- Implement fallback mechanisms where appropriate

### Data Consistency
- Avoid distributed transactions
- Design for eventual consistency
- Implement compensating transactions for rollback scenarios
- Use idempotency keys for critical operations
- Log all state changes for audit trails

## REST API Design

### Endpoint Conventions
- Use plural nouns for resource names: `/customers`, `/products`, `/orders`
- Use HTTP methods appropriately:
  - GET for retrieval
  - POST for creation
  - PUT for full updates
  - PATCH for partial updates
  - DELETE for deletion
- Use path parameters for resource IDs: `/customers/{id}`
- Use query parameters for filtering and pagination: `/products?category=Electronics&page=0&size=20`

### Request/Response DTOs
- Create separate DTOs for requests and responses
- Never expose entity classes directly in APIs
- Use validation annotations (`@NotNull`, `@NotBlank`, `@Email`, `@Min`, `@Max`)
- Use meaningful DTO names: `CreateCustomerRequest`, `CustomerResponse`, `UpdateOrderRequest`

### Status Codes and Responses
- 200 OK: Successful GET, PUT, PATCH
- 201 Created: Successful POST with resource creation
- 204 No Content: Successful DELETE
- 400 Bad Request: Validation failures
- 404 Not Found: Resource not found
- 409 Conflict: Business rule violation
- 500 Internal Server Error: Unexpected server errors
- 503 Service Unavailable: Downstream service unavailable

## Logging and Monitoring

### Logging Standards
- Use SLF4J with Lombok's `@Slf4j`
- Use appropriate log levels:
  - ERROR: Application errors that need immediate attention
  - WARN: Potentially harmful situations
  - INFO: Informational messages highlighting application progress
  - DEBUG: Detailed information for debugging
- Include correlation IDs for distributed tracing
- Log method entry/exit for critical operations
- Never log sensitive information (passwords, credit cards, etc.)

### Logging Examples
```java
log.debug("Fetching customer with id: {}", customerId);
log.info("Created order {} for customer {}", orderId, customerId);
log.warn("Low stock alert for product: {}", productId);
log.error("Failed to connect to inventory service", exception);
```

## Dependency Injection and Loose Coupling

### Constructor Injection
Always use constructor injection:
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final InventoryClient inventoryClient;
}
```

### Interface-Based Design
- Define interfaces for service layer
- Program to interfaces, not implementations
- Makes testing and mocking easier
- Enables future refactoring

## Database and JPA Best Practices

### Entity Design
- Use `@Entity`, `@Table`, `@Column` annotations
- Use `@GeneratedValue` for auto-generated IDs
- Use `@CreatedDate`, `@LastModifiedDate` for audit fields
- Use appropriate fetch types (`LAZY` vs `EAGER`)
- Define proper cascade types for relationships

### Repository Design
- Extend `JpaRepository<Entity, ID>`
- Use Spring Data JPA query methods
- Use `@Query` for complex queries
- Add custom repository methods when needed

## Documentation Requirements

### Code Documentation
- Add JavaDoc for all public APIs
- Document complex business logic
- Add inline comments for non-obvious code
- Keep documentation up to date with code changes

### API Documentation
- Consider using SpringDoc OpenAPI (Swagger) for API documentation
- Document all endpoints with descriptions
- Include request/response examples
- Document error responses

## Security Considerations

### Input Validation
- Validate all user input using Bean Validation
- Sanitize input to prevent injection attacks
- Use `@Valid` annotation in controllers
- Validate business rules in service layer

### Sensitive Data
- Never log passwords, tokens, or sensitive data
- Use environment variables for secrets
- Encrypt sensitive data at rest and in transit

---

## Quick Reference Checklist

When writing code, ensure:
- [ ] No hardcoded configuration values
- [ ] All configuration in `application.properties`
- [ ] Constructor injection used
- [ ] JUnit 5 tests created
- [ ] Tests follow AAA pattern
- [ ] Proper exception handling
- [ ] Appropriate logging added
- [ ] JavaDoc for public APIs
- [ ] Input validation implemented
- [ ] Proper HTTP status codes returned
- [ ] DTOs used instead of entities in APIs
- [ ] External service calls have timeouts
- [ ] No cross-service database access