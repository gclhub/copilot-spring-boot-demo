# Quick Start Guide - Microservices

This guide will help you quickly get the three microservices up and running.

## Prerequisites

- Java 17+
- Maven 3.6+

## Quick Start (3 Commands)

```bash
# 1. Build all services
./build-all.sh

# 2. Run all services
./run-all.sh

# 3. Test the services
curl http://localhost:8081/api/customers
curl http://localhost:8082/api/products
curl http://localhost:8083/api/orders
```

## Create Your First Order

Once all services are running, create an order:

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

## Verify the Order

```bash
# Get all orders
curl http://localhost:8083/api/orders

# Check that inventory was reduced
curl http://localhost:8082/api/products/1
```

## Service URLs

- **Customer Service**: http://localhost:8081
- **Inventory Service**: http://localhost:8082  
- **Order Service**: http://localhost:8083

## H2 Database Consoles

- **Customer DB**: http://localhost:8081/h2-console (JDBC URL: `jdbc:h2:mem:customerdb`)
- **Inventory DB**: http://localhost:8082/h2-console (JDBC URL: `jdbc:h2:mem:inventorydb`)
- **Order DB**: http://localhost:8083/h2-console (JDBC URL: `jdbc:h2:mem:orderdb`)

Username: `sa`, Password: (blank)

## Run Tests

```bash
./test-all.sh
```

## Stop Services

Press `Ctrl+C` in each terminal window running the services.

## Troubleshooting

**Port already in use?**
```bash
# Check what's using the port (Mac/Linux)
lsof -i :8081
lsof -i :8082
lsof -i :8083

# Kill the process
kill -9 <PID>
```

**Services won't start?**
- Ensure Java 17+ is installed: `java -version`
- Ensure Maven is installed: `mvn -version`
- Build services first: `./build-all.sh`

For more details, see [MICROSERVICES_README.md](MICROSERVICES_README.md)
