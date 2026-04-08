# Order Service

## Overview
The Order Service is a microservice responsible for managing orders in the e-commerce application. It coordinates with Customer Service and Inventory Service via REST APIs.

## Port
8083

## Database
H2 in-memory database: `orderdb`

## Dependencies
- **Customer Service**: http://localhost:8081 (for customer validation)
- **Inventory Service**: http://localhost:8082 (for stock management)

## Endpoints

### Get All Orders
```
GET /api/orders
```

### Get Order by ID
```
GET /api/orders/{id}
```

### Get Order by Order Number
```
GET /api/orders/order-number/{orderNumber}
```

### Get Orders by Customer ID
```
GET /api/orders/customer/{customerId}
```

### Get Orders by Status
```
GET /api/orders/status/{status}
```
Status values: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED

### Create Order
```
POST /api/orders
Content-Type: application/json

{
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
}
```

### Update Order Status
```
PATCH /api/orders/{id}/status?status=SHIPPED
```

### Delete Order
```
DELETE /api/orders/{id}
```

## Inter-Service Communication

### Customer Validation
When creating an order, the service calls Customer Service to validate that the customer exists:
```
GET http://localhost:8081/api/customers/{customerId}
```

### Stock Management
The service interacts with Inventory Service to:

1. **Check Product Availability**
   ```
   GET http://localhost:8082/api/products/{productId}
   ```

2. **Reserve Stock** (when creating an order)
   ```
   POST http://localhost:8082/api/products/{productId}/reserve?quantity={qty}
   ```

3. **Restore Stock** (when canceling or deleting an order)
   ```
   POST http://localhost:8082/api/products/{productId}/restore?quantity={qty}
   ```

## Error Handling

The service implements comprehensive error handling:

- **Customer Not Found**: Returns 400 Bad Request
- **Product Not Found**: Returns 400 Bad Request
- **Insufficient Stock**: Returns 400 Bad Request
- **Service Unavailable**: Handles connection errors gracefully

## Compensating Transactions

The service implements compensating transactions to maintain consistency:

1. **Order Creation Failure**: If order creation fails after reserving stock, the stock is automatically restored
2. **Order Cancellation**: When an order is cancelled, all reserved stock is restored
3. **Order Deletion**: When an order is deleted (if not already cancelled), stock is restored

## Running the Service

### Prerequisites
Ensure Customer Service and Inventory Service are running:
1. Start Customer Service on port 8081
2. Start Inventory Service on port 8082
3. Then start Order Service

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

### Test
```bash
mvn test
```

## Configuration
See `src/main/resources/application.properties` for configuration options, including:
- `customer.service.url`: URL of Customer Service
- `inventory.service.url`: URL of Inventory Service

## H2 Console
Access at: http://localhost:8083/h2-console
- JDBC URL: `jdbc:h2:mem:orderdb`
- Username: `sa`
- Password: (empty)

## Sample Data
No initial data. Orders are created via the API.

## Example Workflow

1. **Verify Customer Service is Running**
   ```bash
   curl http://localhost:8081/api/customers/1
   ```

2. **Verify Inventory Service is Running**
   ```bash
   curl http://localhost:8082/api/products/1
   ```

3. **Create an Order**
   ```bash
   curl -X POST http://localhost:8083/api/orders \
     -H "Content-Type: application/json" \
     -d '{
       "customerId": 1,
       "items": [
         {"productId": 1, "quantity": 2}
       ],
       "shippingAddress": "123 Main St",
       "shippingCity": "New York",
       "shippingState": "NY",
       "shippingZip": "10001",
       "shippingCountry": "USA"
     }'
   ```

4. **Check Order Status**
   ```bash
   curl http://localhost:8083/api/orders/1
   ```

5. **Update Order Status**
   ```bash
   curl -X PATCH "http://localhost:8083/api/orders/1/status?status=SHIPPED"
   ```
