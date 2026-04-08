# E-Commerce Microservices

This project demonstrates the refactoring of a monolithic e-commerce application into three independent microservices using Spring Boot 3.2.0 and Java 17.

## Architecture Overview

The application has been split into three microservices:

1. **Customer Service** (Port 8081) - Manages customer information
2. **Inventory Service** (Port 8082) - Manages product inventory
3. **Order Service** (Port 8083) - Manages orders and coordinates with other services

## Microservices Description

### Customer Service
- **Port**: 8081
- **Database**: H2 in-memory database (customerdb)
- **Responsibilities**:
  - Customer CRUD operations
  - Customer validation for order processing
  - Customer data management

### Inventory Service
- **Port**: 8082
- **Database**: H2 in-memory database (inventorydb)
- **Responsibilities**:
  - Product CRUD operations
  - Stock management (reserve/restore)
  - Inventory tracking

### Order Service
- **Port**: 8083
- **Database**: H2 in-memory database (orderdb)
- **Responsibilities**:
  - Order CRUD operations
  - Order processing workflow
  - Coordination with Customer and Inventory services via REST APIs

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Ports 8081, 8082, 8083 available

## Building the Services

Each service can be built independently:

```bash
# Build Customer Service
cd customer-service
mvn clean install

# Build Inventory Service
cd inventory-service
mvn clean install

# Build Order Service
cd order-service
mvn clean install
```

Or build all services from the root directory:

```bash
# Build all services
for service in customer-service inventory-service order-service; do
    cd $service
    mvn clean install
    cd ..
done
```

## Running the Services

The services must be started in the following order to ensure dependencies are available:

### 1. Start Customer Service
```bash
cd customer-service
mvn spring-boot:run
```

### 2. Start Inventory Service
```bash
cd inventory-service
mvn spring-boot:run
```

### 3. Start Order Service
```bash
cd order-service
mvn spring-boot:run
```

Each service will start on its designated port:
- Customer Service: http://localhost:8081
- Inventory Service: http://localhost:8082
- Order Service: http://localhost:8083

## Testing the Services

Run tests for each service:

```bash
# Test Customer Service
cd customer-service
mvn test

# Test Inventory Service
cd inventory-service
mvn test

# Test Order Service
cd order-service
mvn test
```

## API Documentation

### Customer Service API (Port 8081)

#### Endpoints

- `GET /api/customers` - Get all customers
- `GET /api/customers/{id}` - Get customer by ID
- `GET /api/customers/email/{email}` - Get customer by email
- `POST /api/customers` - Create a new customer
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer

#### Example: Create Customer
```bash
curl -X POST http://localhost:8081/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Williams",
    "email": "alice.williams@example.com",
    "phone": "555-4321",
    "address": "789 Elm St",
    "city": "Boston",
    "state": "MA",
    "zipCode": "02101",
    "country": "USA"
  }'
```

### Inventory Service API (Port 8082)

#### Endpoints

- `GET /api/products` - Get all products
- `GET /api/products?activeOnly=true` - Get active products only
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/sku/{sku}` - Get product by SKU
- `GET /api/products/category/{category}` - Get products by category
- `GET /api/products/low-stock?threshold=10` - Get low stock products
- `POST /api/products` - Create a new product
- `PUT /api/products/{id}` - Update product
- `POST /api/products/{id}/reserve?quantity={qty}` - Reserve stock
- `POST /api/products/{id}/restore?quantity={qty}` - Restore stock
- `DELETE /api/products/{id}` - Delete product

#### Example: Check Product
```bash
curl http://localhost:8082/api/products/1
```

#### Example: Reserve Stock
```bash
curl -X POST http://localhost:8082/api/products/1/reserve?quantity=5
```

### Order Service API (Port 8083)

#### Endpoints

- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/order-number/{orderNumber}` - Get order by order number
- `GET /api/orders/customer/{customerId}` - Get orders by customer ID
- `GET /api/orders/status/{status}` - Get orders by status
- `POST /api/orders` - Create a new order
- `PATCH /api/orders/{id}/status?status={STATUS}` - Update order status
- `DELETE /api/orders/{id}` - Delete order

#### Example: Create Order
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2
      },
      {
        "productId": 2,
        "quantity": 1
      }
    ],
    "shippingAddress": "123 Main St",
    "shippingCity": "New York",
    "shippingState": "NY",
    "shippingZip": "10001",
    "shippingCountry": "USA"
  }'
```

#### Example: Update Order Status
```bash
curl -X PATCH "http://localhost:8083/api/orders/1/status?status=SHIPPED"
```

## Database Access

Each service has its own H2 console accessible at:
- Customer Service: http://localhost:8081/h2-console
- Inventory Service: http://localhost:8082/h2-console
- Order Service: http://localhost:8083/h2-console

Connection details:
- **JDBC URL**: See application.properties for each service
- **Username**: sa
- **Password**: (empty)

## Configuration

All configuration is externalized in `application.properties` files:

### Customer Service Configuration
- Database name: `customerdb`
- Server port: `8081`

### Inventory Service Configuration
- Database name: `inventorydb`
- Server port: `8082`

### Order Service Configuration
- Database name: `orderdb`
- Server port: `8083`
- Customer Service URL: `http://localhost:8081`
- Inventory Service URL: `http://localhost:8082`

## Inter-Service Communication

The Order Service communicates with Customer and Inventory services using Spring's `RestClient`:

1. **Customer Validation**: Order Service calls Customer Service to validate customer existence
2. **Stock Management**: Order Service calls Inventory Service to:
   - Check product availability
   - Reserve stock when creating orders
   - Restore stock when canceling or deleting orders

### Error Handling

The services implement proper error handling:
- **Service Unavailable**: Returns appropriate error messages when dependent services are down
- **Resource Not Found**: Returns 404 when resources don't exist
- **Business Logic Errors**: Returns 400 with meaningful error messages

### Compensating Transactions

The Order Service implements compensating transactions:
- If order creation fails after reserving stock, the stock is automatically restored
- When an order is cancelled, the reserved stock is restored
- When an order is deleted, the stock is restored (if not already cancelled)

## Sample Data

Each service initializes with sample data:

### Customer Service
- 3 sample customers (John Doe, Jane Smith, Bob Johnson)

### Inventory Service
- 6 sample products (Laptop, Mouse, Keyboard, Chair, Desk, Webcam)

### Order Service
- No initial data (orders are created via API)

## Architecture Patterns Implemented

1. **Microservices Architecture**: Each domain is isolated in its own service
2. **Database per Service**: Each service has its own database
3. **REST API Communication**: Services communicate via HTTP REST APIs
4. **Externalized Configuration**: All configuration in properties files
5. **Compensating Transactions**: Rollback mechanisms for failed operations
6. **Error Handling**: Proper error handling for inter-service communication

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database
- Spring Web (REST)
- Lombok
- JUnit 5
- Mockito

## Project Structure

```
.
├── customer-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fisglobal/demo/customer/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   └── CustomerServiceApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   └── pom.xml
│
├── inventory-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fisglobal/demo/inventory/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   └── InventoryServiceApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   └── pom.xml
│
└── order-service/
    ├── src/
    │   ├── main/
    │   │   ├── java/com/fisglobal/demo/order/
    │   │   │   ├── client/
    │   │   │   ├── config/
    │   │   │   ├── controller/
    │   │   │   ├── dto/
    │   │   │   ├── model/
    │   │   │   ├── repository/
    │   │   │   ├── service/
    │   │   │   └── OrderServiceApplication.java
    │   │   └── resources/
    │   │       └── application.properties
    │   └── test/
    └── pom.xml
```

## Troubleshooting

### Port Already in Use
If you get a "port already in use" error, either:
1. Stop the process using that port
2. Change the port in `application.properties`

### Service Connection Errors
If the Order Service can't connect to Customer or Inventory services:
1. Ensure Customer and Inventory services are running
2. Check the URLs in Order Service's `application.properties`
3. Verify network connectivity

### Database Errors
H2 databases are in-memory and reset on restart. This is expected behavior for the demo.

## Future Enhancements

Potential improvements for production use:
- Service Discovery (Eureka, Consul)
- API Gateway (Spring Cloud Gateway)
- Circuit Breaker (Resilience4j)
- Distributed Tracing (Zipkin, Jaeger)
- Centralized Configuration (Spring Cloud Config)
- Message-Based Communication (RabbitMQ, Kafka)
- Persistent Databases (PostgreSQL, MySQL)
- Container Orchestration (Docker, Kubernetes)

## License

This project is a demonstration of microservices architecture patterns for educational purposes.
