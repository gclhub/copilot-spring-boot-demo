# Inventory Service

## Overview
The Inventory Service is a microservice responsible for managing product inventory in the e-commerce application.

## Port
8082

## Database
H2 in-memory database: `inventorydb`

## Endpoints

### Get All Products
```
GET /api/products
GET /api/products?activeOnly=true
```

### Get Product by ID
```
GET /api/products/{id}
```

### Get Product by SKU
```
GET /api/products/sku/{sku}
```

### Get Products by Category
```
GET /api/products/category/{category}
```

### Get Low Stock Products
```
GET /api/products/low-stock?threshold=10
```

### Create Product
```
POST /api/products
Content-Type: application/json

{
  "name": "Laptop Computer",
  "description": "High-performance laptop",
  "sku": "LAPTOP-001",
  "price": 1299.99,
  "stockQuantity": 50,
  "category": "Electronics",
  "reorderLevel": 10,
  "active": true
}
```

### Update Product
```
PUT /api/products/{id}
Content-Type: application/json

{
  "name": "Laptop Computer",
  "description": "High-performance laptop",
  "sku": "LAPTOP-001",
  "price": 1299.99,
  "stockQuantity": 50,
  "category": "Electronics",
  "reorderLevel": 10,
  "active": true
}
```

### Reserve Stock
```
POST /api/products/{id}/reserve?quantity=5
```
Used by Order Service when creating orders.

### Restore Stock
```
POST /api/products/{id}/restore?quantity=5
```
Used by Order Service when canceling or deleting orders.

### Delete Product
```
DELETE /api/products/{id}
```

## Running the Service

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

## Sample Data
The service initializes with 6 sample products:
- Laptop Computer (LAPTOP-001)
- Wireless Mouse (MOUSE-001)
- Mechanical Keyboard (KEYBOARD-001)
- Office Chair (CHAIR-001)
- Standing Desk (DESK-001)
- Webcam HD (WEBCAM-001)

## Configuration
See `src/main/resources/application.properties` for configuration options.

## H2 Console
Access at: http://localhost:8082/h2-console
- JDBC URL: `jdbc:h2:mem:inventorydb`
- Username: `sa`
- Password: (empty)
