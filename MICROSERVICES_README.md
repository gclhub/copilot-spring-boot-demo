# Microservices Architecture - E-Commerce Application

This project demonstrates the refactoring of a monolithic Spring Boot application into three independent microservices following best practices for microservices architecture.

## Architecture Overview

The application has been split into three independently deployable microservices:

### 1. Customer Service (Port 8081)
- **Purpose**: Manages customer information and operations
- **Responsibilities**:
  - Customer CRUD operations
  - Customer validation for other services
  - Customer data persistence
- **Database**: H2 in-memory database (`customerdb`)
- **Key Endpoints**:
  - `GET /api/customers` - Get all customers
  - `GET /api/customers/{id}` - Get customer by ID
  - `GET /api/customers/{id}/exists` - Validate customer exists (for inter-service calls)
  - `POST /api/customers` - Create new customer
  - `PUT /api/customers/{id}` - Update customer
  - `DELETE /api/customers/{id}` - Delete customer

### 2. Inventory Service (Port 8082)
- **Purpose**: Manages product inventory and stock operations
- **Responsibilities**:
  - Product CRUD operations
  - Stock reservation and restoration
  - Inventory level tracking
- **Database**: H2 in-memory database (`inventorydb`)
- **Key Endpoints**:
  - `GET /api/products` - Get all products
  - `GET /api/products/{id}` - Get product by ID
  - `POST /api/products/{id}/reserve?quantity={qty}` - Reserve stock (for inter-service calls)
  - `POST /api/products/{id}/restore?quantity={qty}` - Restore stock (for inter-service calls)
  - `POST /api/products` - Create new product
  - `PUT /api/products/{id}` - Update product
  - `DELETE /api/products/{id}` - Delete product

### 3. Order Service (Port 8083)
- **Purpose**: Manages order processing and coordinates with other services
- **Responsibilities**:
  - Order CRUD operations
  - Customer validation via Customer Service
  - Inventory reservation via Inventory Service
  - Compensating transactions on failures
- **Database**: H2 in-memory database (`orderdb`)
- **Key Endpoints**:
  - `GET /api/orders` - Get all orders
  - `GET /api/orders/{id}` - Get order by ID
  - `GET /api/orders/customer/{customerId}` - Get orders by customer
  - `POST /api/orders` - Create new order
  - `PATCH /api/orders/{id}/status?status={status}` - Update order status
  - `DELETE /api/orders/{id}` - Delete order

## Key Design Patterns

### Inter-Service Communication
- **Pattern**: RESTful HTTP APIs
- **Implementation**: RestTemplate with configurable timeouts
- **Error Handling**: Custom exceptions with proper HTTP status codes
- **Resilience**: Compensating transactions for failed operations

### Configuration Externalization
All configuration values are externalized to `application.properties`:
- Service ports: `server.port`
- Database URLs: `spring.datasource.url`
- External service URLs: `customer.service.url`, `inventory.service.url`
- Timeouts: `http.client.connect.timeout`, `http.client.read.timeout`

### Data Consistency
- **Approach**: Eventual consistency
- **Transactions**: Local transactions within each service
- **Compensation**: Explicit compensating transactions (e.g., restoring inventory on order cancellation)
- **No distributed transactions**: Saga pattern for multi-service workflows

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Build Tool**: Maven
- **Database**: H2 (in-memory)
- **Testing**: JUnit 5, Mockito
- **Logging**: SLF4J with Logback

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Building the Services

### Build All Services
```bash
# From the project root
./build-all.sh
```

### Build Individual Services
```bash
# Customer Service
cd customer-service
mvn clean install

# Inventory Service
cd inventory-service
mvn clean install

# Order Service
cd order-service
mvn clean install
```

## Running the Services

### Important: Start Order

The services must be started in the following order to ensure proper inter-service communication:

1. **Customer Service** (Port 8081)
2. **Inventory Service** (Port 8082)
3. **Order Service** (Port 8083)

### Option 1: Run All Services (Recommended)
```bash
# From the project root
./run-all.sh
```

This script will:
- Start all three services in the correct order
- Run each service in a separate terminal window
- Display startup logs for each service

### Option 2: Run Services Individually

**Terminal 1 - Customer Service:**
```bash
cd customer-service
mvn spring-boot:run
```

**Terminal 2 - Inventory Service:**
```bash
cd inventory-service
mvn spring-boot:run
```

**Terminal 3 - Order Service:**
```bash
cd order-service
mvn spring-boot:run
```

### Option 3: Run as JAR Files
```bash
# Terminal 1
java -jar customer-service/target/customer-service-1.0.0-SNAPSHOT.jar

# Terminal 2
java -jar inventory-service/target/inventory-service-1.0.0-SNAPSHOT.jar

# Terminal 3
java -jar order-service/target/order-service-1.0.0-SNAPSHOT.jar
```

## Running Tests

### Run All Tests
```bash
# From project root
./test-all.sh
```

### Run Tests for Individual Services
```bash
# Customer Service
cd customer-service
mvn test

# Inventory Service
cd inventory-service
mvn test

# Order Service
cd order-service
mvn test
```

## Testing the Services

### Sample Workflow

1. **Check Available Customers:**
```bash
curl http://localhost:8081/api/customers
```

2. **Check Available Products:**
```bash
curl http://localhost:8082/api/products
```

3. **Create an Order:**
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ],
    "shippingAddress": "123 Main St",
    "shippingCity": "New York",
    "shippingState": "NY",
    "shippingZip": "10001",
    "shippingCountry": "USA"
  }'
```

4. **Check Order Status:**
```bash
curl http://localhost:8083/api/orders/1
```

5. **Verify Inventory Was Reserved:**
```bash
curl http://localhost:8082/api/products/1
# Check that stockQuantity was reduced
```

## Configuration

### Environment Variables

Each service can be configured using environment variables:

**Customer Service:**
- `CUSTOMER_SERVICE_PORT` - Service port (default: 8081)
- `CUSTOMER_DB_URL` - Database URL
- `LOG_LEVEL` - Logging level (default: DEBUG)

**Inventory Service:**
- `INVENTORY_SERVICE_PORT` - Service port (default: 8082)
- `INVENTORY_DB_URL` - Database URL
- `LOG_LEVEL` - Logging level (default: DEBUG)

**Order Service:**
- `ORDER_SERVICE_PORT` - Service port (default: 8083)
- `ORDER_DB_URL` - Database URL
- `CUSTOMER_SERVICE_URL` - Customer Service URL (default: http://localhost:8081)
- `INVENTORY_SERVICE_URL` - Inventory Service URL (default: http://localhost:8082)
- `HTTP_CLIENT_CONNECT_TIMEOUT` - Connection timeout in ms (default: 5000)
- `HTTP_CLIENT_READ_TIMEOUT` - Read timeout in ms (default: 5000)
- `LOG_LEVEL` - Logging level (default: DEBUG)

### Example: Running with Custom Ports
```bash
# Customer Service on port 9081
CUSTOMER_SERVICE_PORT=9081 mvn spring-boot:run -f customer-service/pom.xml

# Inventory Service on port 9082
INVENTORY_SERVICE_PORT=9082 mvn spring-boot:run -f inventory-service/pom.xml

# Order Service on port 9083 (with updated service URLs)
ORDER_SERVICE_PORT=9083 \
CUSTOMER_SERVICE_URL=http://localhost:9081 \
INVENTORY_SERVICE_URL=http://localhost:9082 \
mvn spring-boot:run -f order-service/pom.xml
```

## H2 Database Console

Each service exposes an H2 console for database inspection:

- **Customer Service**: http://localhost:8081/h2-console
  - JDBC URL: `jdbc:h2:mem:customerdb`
- **Inventory Service**: http://localhost:8082/h2-console
  - JDBC URL: `jdbc:h2:mem:inventorydb`
- **Order Service**: http://localhost:8083/h2-console
  - JDBC URL: `jdbc:h2:mem:orderdb`

Username: `sa`
Password: (leave blank)

## Project Structure

```
.
├── customer-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fisglobal/customer/
│   │   │   │   ├── config/          # Configuration classes
│   │   │   │   ├── controller/      # REST controllers
│   │   │   │   ├── model/           # JPA entities
│   │   │   │   ├── repository/      # Data repositories
│   │   │   │   ├── service/         # Business logic
│   │   │   │   └── CustomerServiceApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/com/fisglobal/customer/
│   └── pom.xml
│
├── inventory-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fisglobal/inventory/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   └── InventoryServiceApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/com/fisglobal/inventory/
│   └── pom.xml
│
├── order-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fisglobal/order/
│   │   │   │   ├── client/          # REST clients for external services
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/             # Data transfer objects
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   └── OrderServiceApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/com/fisglobal/order/
│   └── pom.xml
│
├── build-all.sh              # Build all services
├── run-all.sh                # Run all services
├── test-all.sh               # Test all services
└── README.md                 # This file
```

## Development Guidelines

### Code Style
- Follow Java best practices and Oracle coding conventions
- Use Java 17 features where appropriate
- Maximum line length: 120 characters
- Use 4 spaces for indentation

### Testing
- Write JUnit 5 tests for all service methods
- Use Mockito for mocking dependencies
- Follow AAA (Arrange-Act-Assert) pattern
- Use `@DisplayName` for readable test descriptions
- Mock external service calls in tests

### Configuration
- Never hardcode configuration values
- Externalize all configuration to `application.properties`
- Use environment variable placeholders with defaults
- Document all configuration properties

### Error Handling
- Return appropriate HTTP status codes
- Log errors with sufficient context
- Handle service unavailability gracefully
- Implement compensating transactions where needed

## Troubleshooting

### Service Won't Start
- **Check if port is already in use**: `lsof -i :8081` (Mac/Linux) or `netstat -ano | findstr :8081` (Windows)
- **Verify Java version**: `java -version` (should be 17+)
- **Check Maven installation**: `mvn -version`

### Order Creation Fails
- **Ensure Customer Service is running**: `curl http://localhost:8081/api/customers/1`
- **Ensure Inventory Service is running**: `curl http://localhost:8082/api/products/1`
- **Check service URLs in Order Service configuration**
- **Review Order Service logs for detailed error messages**

### Tests Failing
- **Run `mvn clean test` to ensure fresh build**
- **Check that all dependencies are properly mocked**
- **Verify test data setup in `@BeforeEach` methods**

## Future Enhancements

- **Service Discovery**: Implement Eureka or Consul for dynamic service discovery
- **API Gateway**: Add Spring Cloud Gateway for unified entry point
- **Circuit Breakers**: Implement Resilience4j for fault tolerance
- **Distributed Tracing**: Add Sleuth and Zipkin for request tracing
- **Message Queues**: Use RabbitMQ or Kafka for asynchronous communication
- **Authentication**: Implement OAuth2/JWT for security
- **Monitoring**: Add Prometheus and Grafana for metrics
- **Container Deployment**: Create Docker images and Kubernetes manifests

## License

This project is for demonstration purposes.

## Contact

For questions or issues, please refer to the project documentation or create an issue in the repository.
