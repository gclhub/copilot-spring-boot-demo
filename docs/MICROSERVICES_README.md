# Microservices Implementation

This branch (`completed-demo`) contains the fully refactored microservices architecture extracted from the monolith.

## Architecture Overview

The monolith has been split into three independent microservices:

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Customer   │     │  Inventory   │     │    Order     │
│   Service    │     │   Service    │     │   Service    │
│  Port 8081   │     │  Port 8082   │     │  Port 8083   │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                     │
       │                    │                     │
       ▼                    ▼                     ▼
  customer_db          inventory_db          order_db
```

## Services

### 1. Customer Service (Port 8081)
**Location**: `customer-service/`

**Responsibilities**:
- Customer CRUD operations
- Customer data management
- Independent database

**Endpoints**:
- GET `/api/customers` - List all customers
- GET `/api/customers/{id}` - Get customer by ID
- POST `/api/customers` - Create new customer
- PUT `/api/customers/{id}` - Update customer
- DELETE `/api/customers/{id}` - Delete customer

**Database**: H2 (customerdb)

### 2. Inventory Service (Port 8082)
**Location**: `inventory-service/`

**Responsibilities**:
- Product catalog management
- Stock management
- Reserve/restore inventory operations

**Endpoints**:
- GET `/api/products` - List all products
- GET `/api/products/{id}` - Get product by ID
- POST `/api/products` - Create new product
- PUT `/api/products/{id}` - Update product
- POST `/api/products/{id}/reserve` - Reserve stock
- POST `/api/products/{id}/restore` - Restore stock
- DELETE `/api/products/{id}` - Delete product

**Database**: H2 (inventorydb)

### 3. Order Service (Port 8083)
**Location**: `order-service/`

**Responsibilities**:
- Order processing and orchestration
- Calls Customer Service for validation
- Calls Inventory Service for stock management

**Endpoints**:
- GET `/api/orders` - List all orders
- GET `/api/orders/{id}` - Get order by ID
- POST `/api/orders` - Create new order
- PUT `/api/orders/{id}/cancel` - Cancel order
- PUT `/api/orders/{id}/status` - Update order status

**Database**: H2 (orderdb)

**Dependencies**:
- Customer Service (validates customer exists)
- Inventory Service (checks stock, reserves/restores inventory)

## Running the Microservices

### Prerequisites
- Java 17
- Maven 3.6+

### Option 1: Run All Services Manually

**Terminal 1 - Customer Service**:
```bash
cd customer-service
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
```

**Terminal 2 - Inventory Service**:
```bash
cd inventory-service
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
```

**Terminal 3 - Order Service**:
```bash
cd order-service
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
```

### Option 2: Use the Startup Script

```bash
./start-all-services.sh
```

### Option 3: Build and Run JARs

```bash
# Build all services
./build-all-services.sh

# Run services
java -jar customer-service/target/customer-service-1.0.0-SNAPSHOT.jar &
java -jar inventory-service/target/inventory-service-1.0.0-SNAPSHOT.jar &
java -jar order-service/target/order-service-1.0.0-SNAPSHOT.jar &
```

## Testing the Microservices

### 1. Test Customer Service (8081)

```bash
# Get all customers
curl http://localhost:8081/api/customers | jq

# Get specific customer
curl http://localhost:8081/api/customers/1 | jq
```

### 2. Test Inventory Service (8082)

```bash
# Get all products
curl http://localhost:8082/api/products | jq

# Get specific product
curl http://localhost:8082/api/products/1 | jq

# Reserve stock
curl -X POST "http://localhost:8082/api/products/1/reserve?quantity=5"

# Restore stock
curl -X POST "http://localhost:8082/api/products/1/restore?quantity=5"
```

### 3. Test Order Service (8083)

```bash
# Create an order (calls Customer + Inventory services)
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {"productId": 1, "quantity": 1},
      {"productId": 2, "quantity": 2}
    ],
    "shippingAddress": "123 Main St",
    "shippingCity": "New York",
    "shippingState": "NY",
    "shippingZip": "10001",
    "shippingCountry": "USA"
  }' | jq

# Get all orders
curl http://localhost:8083/api/orders | jq

# Cancel an order
curl -X PUT http://localhost:8083/api/orders/1/cancel | jq
```

## Key Microservices Patterns Implemented

### 1. Database per Service
Each service has its own database:
- `customerdb` for Customer Service
- `inventorydb` for Inventory Service
- `orderdb` for Order Service

### 2. Synchronous Communication (REST)
- Order Service calls Customer Service via REST
- Order Service calls Inventory Service via REST
- Uses `RestTemplate` for HTTP communication

### 3. Service Isolation
- Each service runs independently
- Each service can be deployed separately
- Services communicate only via APIs

### 4. Compensating Transactions
- Stock reservation rollback if order creation fails
- Stock restoration when order is cancelled

### 5. Data Consistency
- Eventual consistency between services
- Compensating actions for failure scenarios

## Differences from Monolith

| Aspect | Monolith | Microservices |
|--------|----------|---------------|
| **Deployment** | Single application | 3 separate applications |
| **Database** | Shared database | Database per service |
| **Communication** | Direct method calls | REST API calls |
| **Scaling** | Scale entire app | Scale services independently |
| **Technology** | Same stack | Can use different stacks |
| **Ports** | Single port (8080) | Multiple ports (8081-8083) |
| **Dependencies** | Direct Java dependencies | HTTP clients |
| **Transactions** | ACID transactions | Eventual consistency |

## Microservices Challenges Addressed

### 1. Service Discovery
Currently using hardcoded URLs in `application.properties`:
```properties
customer.service.url=http://localhost:8081
inventory.service.url=http://localhost:8082
```

**Production improvement**: Use service discovery (Eureka, Consul)

### 2. Distributed Transactions
Implemented compensating transactions:
- Stock rollback on order creation failure
- Stock restoration on order cancellation

**Production improvement**: Implement Saga pattern

### 3. API Gateway (Not Implemented)
Current state: Direct service access

**Production improvement**: Add API Gateway (Spring Cloud Gateway)

### 4. Circuit Breaker (Not Implemented)
Current state: Direct REST calls without fault tolerance

**Production improvement**: Add Resilience4j circuit breakers

### 5. Distributed Tracing (Not Implemented)
Current state: Individual service logs

**Production improvement**: Add Spring Cloud Sleuth + Zipkin

## Project Structure

```
copilot-spring-boot-demo/
├── customer-service/
│   ├── src/main/java/com/fisglobal/demo/customer/
│   │   ├── CustomerServiceApplication.java
│   │   ├── model/Customer.java
│   │   ├── repository/CustomerRepository.java
│   │   ├── service/CustomerService.java
│   │   ├── controller/CustomerController.java
│   │   └── config/DataInitializer.java
│   ├── src/main/resources/application.properties
│   └── pom.xml
│
├── inventory-service/
│   ├── src/main/java/com/fisglobal/demo/inventory/
│   │   ├── InventoryServiceApplication.java
│   │   ├── model/Product.java
│   │   ├── repository/ProductRepository.java
│   │   ├── service/ProductService.java
│   │   ├── controller/ProductController.java
│   │   └── config/DataInitializer.java
│   ├── src/main/resources/application.properties
│   └── pom.xml
│
├── order-service/
│   ├── src/main/java/com/fisglobal/demo/order/
│   │   ├── OrderServiceApplication.java
│   │   ├── model/{Order.java, OrderItem.java}
│   │   ├── dto/{CreateOrderRequest.java, OrderItemRequest.java}
│   │   ├── client/{CustomerClient.java, InventoryClient.java, DTOs}
│   │   ├── repository/OrderRepository.java
│   │   ├── service/OrderService.java
│   │   └── controller/OrderController.java
│   ├── src/main/resources/application.properties
│   └── pom.xml
│
└── (original monolith in src/)
```

## Migration Path

This branch shows the **end state** after completing the demo. The migration followed these steps:

1. **Analyze Domains** - Identified Customer, Inventory, and Order domains
2. **Extract Customer Service** - First independent service (no dependencies)
3. **Extract Inventory Service** - Second independent service
4. **Extract Order Service** - Dependent service with REST clients
5. **Implement Communication** - RestTemplate clients for inter-service calls
6. **Handle Data** - Database per service pattern
7. **Test Integration** - End-to-end order creation flow

## Performance Considerations

### Network Latency
- **Monolith**: In-memory method calls (<1ms)
- **Microservices**: HTTP calls (10-50ms per call)
- Order creation now involves 2+ network calls

### Mitigation Strategies
1. Caching (Redis/Memcached)
2. Async communication (messaging queues)
3. API composition patterns
4. Database connection pooling

## Next Steps for Production

1. **Service Discovery**: Add Eureka/Consul
2. **API Gateway**: Add Spring Cloud Gateway
3. **Config Server**: Centralize configuration
4. **Circuit Breakers**: Add Resilience4j
5. **Distributed Tracing**: Add Sleuth + Zipkin
6. **Message Queue**: Add RabbitMQ/Kafka for async
7. **Containers**: Dockerize all services
8. **Orchestration**: Deploy to Kubernetes
9. **Monitoring**: Add Prometheus + Grafana
10. **Security**: Add OAuth2/JWT

## Troubleshooting

### Services won't start
- Check Java 17 is installed: `java -version`
- Check ports are free: `lsof -i :8081` (8082, 8083)
- Check JAVA_HOME: `echo $JAVA_HOME`

### Order creation fails
- Ensure Customer Service is running (8081)
- Ensure Inventory Service is running (8082)
- Check service URLs in order-service/application.properties

### Stock not updating
- Check Inventory Service logs
- Verify product IDs exist
- Check stock quantity is sufficient

## Documentation

See the main branch `/docs` folder for:
- DEMO_SCRIPT.md - How this was created
- DIAGRAMS.md - Architecture diagrams
- COPILOT_PROMPTS.md - Prompts used for refactoring

## Switching Between Branches

```bash
# View original monolith
git checkout main

# View refactored microservices
git checkout completed-demo
```

---

**This is a reference implementation for demonstration purposes.**  
Use this as a backup during live demos or for customer experimentation.
