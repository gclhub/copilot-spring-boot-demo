# Microservices API Examples

This document provides comprehensive API examples for testing all three microservices.

## Customer Service API (Port 8081)

### Get All Customers
```bash
curl http://localhost:8081/api/customers
```

### Get Customer by ID
```bash
curl http://localhost:8081/api/customers/1
```

### Get Customer by Email
```bash
curl http://localhost:8081/api/customers/email/john.doe@example.com
```

### Check if Customer Exists (Used by Order Service)
```bash
curl http://localhost:8081/api/customers/1/exists
```

### Create a New Customer
```bash
curl -X POST http://localhost:8081/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Williams",
    "email": "alice.williams@example.com",
    "phone": "555-9999",
    "address": "456 Oak St",
    "city": "Boston",
    "state": "MA",
    "zipCode": "02101",
    "country": "USA"
  }'
```

### Update a Customer
```bash
curl -X PUT http://localhost:8081/api/customers/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe.updated@example.com",
    "phone": "555-1234",
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }'
```

### Delete a Customer
```bash
curl -X DELETE http://localhost:8081/api/customers/1
```

---

## Inventory Service API (Port 8082)

### Get All Products
```bash
curl http://localhost:8082/api/products
```

### Get Active Products Only
```bash
curl "http://localhost:8082/api/products?activeOnly=true"
```

### Get Product by ID
```bash
curl http://localhost:8082/api/products/1
```

### Get Product by SKU
```bash
curl http://localhost:8082/api/products/sku/LAPTOP-001
```

### Get Products by Category
```bash
curl http://localhost:8082/api/products/category/Electronics
```

### Get Low Stock Products
```bash
curl "http://localhost:8082/api/products/low-stock?threshold=20"
```

### Create a New Product
```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "USB-C Cable",
    "description": "High-speed USB-C to USB-C cable",
    "sku": "CABLE-001",
    "price": 19.99,
    "stockQuantity": 150,
    "category": "Electronics",
    "reorderLevel": 25,
    "active": true
  }'
```

### Update a Product
```bash
curl -X PUT http://localhost:8082/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Computer - Updated",
    "description": "High-performance laptop for business and gaming - Updated model",
    "sku": "LAPTOP-001",
    "price": 1399.99,
    "stockQuantity": 45,
    "category": "Electronics",
    "reorderLevel": 10,
    "active": true
  }'
```

### Reserve Stock (Used by Order Service)
```bash
curl -X POST "http://localhost:8082/api/products/1/reserve?quantity=5"
```

### Restore Stock (Used by Order Service on Cancellation)
```bash
curl -X POST "http://localhost:8082/api/products/1/restore?quantity=5"
```

### Delete a Product
```bash
curl -X DELETE http://localhost:8082/api/products/1
```

---

## Order Service API (Port 8083)

### Get All Orders
```bash
curl http://localhost:8083/api/orders
```

### Get Order by ID
```bash
curl http://localhost:8083/api/orders/1
```

### Get Order by Order Number
```bash
# First create an order, then use its order number
curl http://localhost:8083/api/orders/order-number/ORD-1234567890123
```

### Get Orders by Customer ID
```bash
curl http://localhost:8083/api/orders/customer/1
```

### Get Orders by Status
```bash
curl http://localhost:8083/api/orders/status/CONFIRMED
```

Available statuses: `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`

### Create a New Order (Single Item)
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

### Create a New Order (Multiple Items)
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 2,
    "items": [
      {
        "productId": 1,
        "quantity": 1
      },
      {
        "productId": 2,
        "quantity": 3
      },
      {
        "productId": 3,
        "quantity": 1
      }
    ],
    "shippingAddress": "456 Oak Ave",
    "shippingCity": "Los Angeles",
    "shippingState": "CA",
    "shippingZip": "90001",
    "shippingCountry": "USA"
  }'
```

### Update Order Status
```bash
# Change to PROCESSING
curl -X PATCH "http://localhost:8083/api/orders/1/status?status=PROCESSING"

# Change to SHIPPED
curl -X PATCH "http://localhost:8083/api/orders/1/status?status=SHIPPED"

# Change to DELIVERED
curl -X PATCH "http://localhost:8083/api/orders/1/status?status=DELIVERED"

# Cancel order (this will restore inventory)
curl -X PATCH "http://localhost:8083/api/orders/1/status?status=CANCELLED"
```

### Delete an Order (Restores Inventory)
```bash
curl -X DELETE http://localhost:8083/api/orders/1
```

---

## End-to-End Workflow Examples

### Complete Order Flow

**Step 1: Check available customers**
```bash
curl http://localhost:8081/api/customers
# Note the customer ID you want to use
```

**Step 2: Check available products and stock**
```bash
curl http://localhost:8082/api/products
# Note the product IDs and their stock quantities
```

**Step 3: Create an order**
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
# Note the order ID returned
```

**Step 4: Verify inventory was reserved**
```bash
curl http://localhost:8082/api/products/1
# Check that stockQuantity was reduced by 2
```

**Step 5: Update order status**
```bash
curl -X PATCH "http://localhost:8083/api/orders/1/status?status=PROCESSING"
```

**Step 6: Cancel order (restores inventory)**
```bash
curl -X PATCH "http://localhost:8083/api/orders/1/status?status=CANCELLED"
```

**Step 7: Verify inventory was restored**
```bash
curl http://localhost:8082/api/products/1
# Check that stockQuantity was increased by 2
```

### Error Handling Examples

**Attempt to order non-existent product:**
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productId": 999,
        "quantity": 1
      }
    ],
    "shippingAddress": "123 Main St",
    "shippingCity": "New York",
    "shippingState": "NY",
    "shippingZip": "10001",
    "shippingCountry": "USA"
  }'
# Should return 400 Bad Request
```

**Attempt to order with non-existent customer:**
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 999,
    "items": [
      {
        "productId": 1,
        "quantity": 1
      }
    ],
    "shippingAddress": "123 Main St",
    "shippingCity": "New York",
    "shippingState": "NY",
    "shippingZip": "10001",
    "shippingCountry": "USA"
  }'
# Should return 400 Bad Request
```

**Attempt to order more than available stock:**
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 1000
      }
    ],
    "shippingAddress": "123 Main St",
    "shippingCity": "New York",
    "shippingState": "NY",
    "shippingZip": "10001",
    "shippingCountry": "USA"
  }'
# Should return 400 Bad Request with "Insufficient stock" message
```

---

## Testing with Pretty JSON Output

If you have `jq` installed, you can format the JSON output:

```bash
curl http://localhost:8081/api/customers | jq '.'
curl http://localhost:8082/api/products | jq '.'
curl http://localhost:8083/api/orders | jq '.'
```

## Using Postman or Similar Tools

Import these endpoints into Postman or your preferred API client:

**Base URLs:**
- Customer Service: `http://localhost:8081`
- Inventory Service: `http://localhost:8082`
- Order Service: `http://localhost:8083`

Set `Content-Type: application/json` header for all POST and PUT requests.
