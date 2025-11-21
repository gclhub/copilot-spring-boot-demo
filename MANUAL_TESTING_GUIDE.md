# Manual Testing Guide - Microservices

## Service Status

✅ **Customer Service** - Running on port 8081  
✅ **Inventory Service** - Running on port 8082  
⚠️ **Order Service** - Port 8083 (needs investigation)

## Prerequisites

All services should be running. If not, start them with:

```bash
# Build all services first (requires Java 17)
./build-all.sh

# Start all services in separate terminals
./run-all.sh
```

---

## 1. Customer Service Testing (Port 8081)

### Base URL
```
http://localhost:8081/api/customers
```

### Test 1: Get All Customers
```bash
curl http://localhost:8081/api/customers
```

**Expected Result:** Returns 3 customers (John Doe, Jane Smith, Bob Johnson)

**Example Response:**
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "555-1234",
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }
]
```

### Test 2: Get Customer by ID
```bash
curl http://localhost:8081/api/customers/1
```

**Expected Result:** Returns John Doe's details

### Test 3: Get Customer by Email
```bash
curl http://localhost:8081/api/customers/email/jane.smith@example.com
```

**Expected Result:** Returns Jane Smith's details

### Test 4: Create New Customer
```bash
curl -X POST http://localhost:8081/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Williams",
    "email": "alice.williams@example.com",
    "phone": "555-1111",
    "address": "321 Elm St",
    "city": "Boston",
    "state": "MA",
    "zipCode": "02101",
    "country": "USA"
  }'
```

**Expected Result:** Status 201 Created, returns the new customer with ID 4

### Test 5: Update Customer
```bash
curl -X PUT http://localhost:8081/api/customers/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "555-9999",
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }'
```

**Expected Result:** Status 200 OK, phone number updated to 555-9999

### Test 6: Delete Customer
```bash
curl -X DELETE http://localhost:8081/api/customers/4
```

**Expected Result:** Status 204 No Content

### Test 7: Access H2 Database Console
Open in browser:
```
http://localhost:8081/h2-console
```

**Credentials:**
- JDBC URL: `jdbc:h2:mem:customerdb`
- Username: `sa`
- Password: (leave empty)

---

## 2. Inventory Service Testing (Port 8082)

### Base URL
```
http://localhost:8082/api/products
```

### Test 1: Get All Products
```bash
curl http://localhost:8082/api/products
```

**Expected Result:** Returns 6 products (Laptop, Mouse, Keyboard, Office Chair, Standing Desk, Webcam)

**Formatted Output:**
```bash
curl -s http://localhost:8082/api/products | python3 -m json.tool
```

### Test 2: Get Product by ID
```bash
curl http://localhost:8082/api/products/1
```

**Expected Result:** Returns Laptop Computer details

### Test 3: Get Product by SKU
```bash
curl http://localhost:8082/api/products/sku/MOUSE-001
```

**Expected Result:** Returns Wireless Mouse details

### Test 4: Get Products by Category
```bash
curl http://localhost:8082/api/products/category/Electronics
```

**Expected Result:** Returns all Electronics products (should be 3 items)

```bash
curl http://localhost:8082/api/products/category/Furniture
```

**Expected Result:** Returns all Furniture products (should be 2 items)

### Test 5: Get Low Stock Products
```bash
curl http://localhost:8082/api/products/low-stock?threshold=20
```

**Expected Result:** Returns products with stock <= 20

### Test 6: Get Active Products Only
```bash
curl http://localhost:8082/api/products?activeOnly=true
```

**Expected Result:** Returns all active products

### Test 7: Create New Product
```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "USB Cable",
    "description": "USB-C to USB-A cable, 6 feet",
    "sku": "CABLE-001",
    "price": 9.99,
    "stockQuantity": 500,
    "category": "Electronics",
    "reorderLevel": 50,
    "active": true
  }'
```

**Expected Result:** Status 201 Created, returns new product with ID 7

### Test 8: Update Product
```bash
curl -X PUT http://localhost:8082/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Computer",
    "description": "High-performance laptop for business and gaming - UPDATED",
    "sku": "LAPTOP-001",
    "price": 1199.99,
    "stockQuantity": 45,
    "category": "Electronics",
    "reorderLevel": 10,
    "active": true
  }'
```

**Expected Result:** Status 200 OK, price updated to $1,199.99

### Test 9: Reserve Stock (Critical for Order Service)
```bash
curl -X POST "http://localhost:8082/api/products/1/reserve?quantity=2"
```

**Expected Result:** Status 200 OK, stock quantity reduced by 2

**Verify:**
```bash
curl http://localhost:8082/api/products/1 | python3 -m json.tool | grep stockQuantity
```

### Test 10: Restore Stock
```bash
curl -X POST "http://localhost:8082/api/products/1/restore?quantity=2"
```

**Expected Result:** Status 200 OK, stock quantity increased by 2

### Test 11: Delete Product
```bash
curl -X DELETE http://localhost:8082/api/products/7
```

**Expected Result:** Status 204 No Content

### Test 12: Access H2 Database Console
Open in browser:
```
http://localhost:8082/h2-console
```

**Credentials:**
- JDBC URL: `jdbc:h2:mem:inventorydb`
- Username: `sa`
- Password: (leave empty)

---

## 3. Order Service Testing (Port 8083)

⚠️ **Note:** Order Service requires both Customer Service and Inventory Service to be running.

### Base URL
```
http://localhost:8083/api/orders
```

### Test 1: Get All Orders
```bash
curl http://localhost:8083/api/orders
```

**Expected Result:** Returns empty array initially `[]`

### Test 2: Create New Order
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 1
      },
      {
        "productId": 2,
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

**Expected Result:** 
- Status 201 Created
- Order created with a unique order number
- Stock reserved in Inventory Service
- Customer validated via Customer Service

### Test 3: Get Order by ID
```bash
curl http://localhost:8083/api/orders/1
```

**Expected Result:** Returns order details with items

### Test 4: Get Order by Order Number
```bash
curl http://localhost:8083/api/orders/order-number/ORD-001
```

**Expected Result:** Returns order details (if order number matches)

### Test 5: Get Orders by Customer
```bash
curl http://localhost:8083/api/orders/customer/1
```

**Expected Result:** Returns all orders for customer ID 1

### Test 6: Get Orders by Status
```bash
curl http://localhost:8083/api/orders/status/PENDING
```

**Expected Result:** Returns all PENDING orders

```bash
curl http://localhost:8083/api/orders/status/CONFIRMED
```

### Test 7: Update Order Status
```bash
curl -X PATCH "http://localhost:8083/api/orders/1/status?status=CONFIRMED"
```

**Expected Result:** Status 200 OK, order status updated

### Test 8: Cancel Order (Tests Compensating Transaction)
```bash
curl -X DELETE http://localhost:8083/api/orders/1
```

**Expected Result:** 
- Status 204 No Content
- Stock restored in Inventory Service

**Verify Stock Restored:**
```bash
# Check that stock was returned
curl http://localhost:8082/api/products/1 | python3 -m json.tool | grep stockQuantity
curl http://localhost:8082/api/products/2 | python3 -m json.tool | grep stockQuantity
```

### Test 9: Create Order with Insufficient Stock (Error Case)
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
```

**Expected Result:** Status 400 Bad Request, error message about insufficient stock

### Test 10: Create Order with Invalid Customer (Error Case)
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
```

**Expected Result:** Status 404 Not Found, error message about customer not found

### Test 11: Access H2 Database Console
Open in browser:
```
http://localhost:8083/h2-console
```

**Credentials:**
- JDBC URL: `jdbc:h2:mem:orderdb`
- Username: `sa`
- Password: (leave empty)

---

## 4. Integration Testing

### Test Scenario: Complete Order Flow

**Step 1:** Verify initial stock levels
```bash
curl http://localhost:8082/api/products/1 | python3 -m json.tool | grep -A1 "stockQuantity"
```

**Step 2:** Create an order
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 3, "quantity": 1}
    ],
    "shippingAddress": "123 Main St",
    "shippingCity": "New York",
    "shippingState": "NY",
    "shippingZip": "10001",
    "shippingCountry": "USA"
  }'
```

**Step 3:** Verify stock was reserved
```bash
curl http://localhost:8082/api/products/1 | python3 -m json.tool | grep -A1 "stockQuantity"
curl http://localhost:8082/api/products/3 | python3 -m json.tool | grep -A1 "stockQuantity"
```

**Step 4:** Update order status
```bash
curl -X PATCH "http://localhost:8083/api/orders/1/status?status=CONFIRMED"
```

**Step 5:** Verify order in customer's history
```bash
curl http://localhost:8083/api/orders/customer/1
```

---

## 5. Health Checks

### Check if all services are running:
```bash
# Customer Service
curl -I http://localhost:8081/api/customers

# Inventory Service
curl -I http://localhost:8082/api/products

# Order Service
curl -I http://localhost:8083/api/orders
```

All should return `HTTP/1.1 200` or `HTTP/1.1 404` (meaning service is up but route not found)

### Check ports:
```bash
lsof -ti:8081  # Customer Service
lsof -ti:8082  # Inventory Service
lsof -ti:8083  # Order Service
```

---

## 6. Troubleshooting

### Service won't start
```bash
# Check if port is already in use
lsof -ti:8081  # (or 8082, 8083)

# Kill process if needed
lsof -ti:8081 | xargs kill -9

# Check Java version
java -version  # Should be 17.x.x

# Force Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
java -version
```

### Service returns 404
- Service is running but endpoint path is wrong
- Check the exact URL path
- Verify service has started completely (check logs)

### Service returns 500
- Check service logs in the terminal window
- Database might not be initialized
- Check H2 console for data

### Order Service can't reach Customer/Inventory Service
```bash
# Verify other services are running
curl http://localhost:8081/api/customers/1
curl http://localhost:8082/api/products/1

# Check Order Service configuration
cat order-service/src/main/resources/application.properties | grep service.url
```

---

## 7. Testing Summary

### Quick Validation Script
```bash
#!/bin/bash
echo "Testing Customer Service..."
curl -s http://localhost:8081/api/customers | grep -q "John" && echo "✓ Customer Service OK" || echo "✗ Customer Service FAIL"

echo "Testing Inventory Service..."
curl -s http://localhost:8082/api/products | grep -q "Laptop" && echo "✓ Inventory Service OK" || echo "✗ Inventory Service FAIL"

echo "Testing Order Service..."
curl -s http://localhost:8083/api/orders > /dev/null 2>&1 && echo "✓ Order Service OK" || echo "✗ Order Service FAIL"
```

### Expected Test Results
- ✅ All GET endpoints return data
- ✅ POST creates new resources
- ✅ PUT/PATCH updates resources
- ✅ DELETE removes resources
- ✅ Inter-service communication works (Order → Customer, Order → Inventory)
- ✅ Stock reservation/restore works correctly
- ✅ Error handling works for invalid requests

---

## 8. Next Steps

After manual testing is complete:
1. Run unit tests: `./test-all.sh`
2. Review test coverage
3. Test edge cases and error scenarios
4. Performance testing (load testing)
5. Security testing (input validation)

---

## Notes

- All services use H2 in-memory databases (data is lost on restart)
- Each service has its own database (database-per-service pattern)
- Order Service demonstrates REST-based inter-service communication
- Stock reservation uses compensating transactions for rollback
