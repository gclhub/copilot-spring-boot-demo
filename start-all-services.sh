#!/bin/bash

echo "Starting all microservices..."
echo "============================="

# Find Java 17
JAVA17_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo "")

if [ -z "$JAVA17_HOME" ]; then
    echo "Error: Java 17 not found!"
    echo "Please install Java 17:"
    echo "  brew install openjdk@17"
    exit 1
fi

echo "Using Java 17 from: $JAVA17_HOME"
export JAVA_HOME="$JAVA17_HOME"

# Check if services need to be built
if [ ! -f "customer-service/target/customer-service-1.0.0-SNAPSHOT.jar" ] || \
   [ ! -f "inventory-service/target/inventory-service-1.0.0-SNAPSHOT.jar" ] || \
   [ ! -f "order-service/target/order-service-1.0.0-SNAPSHOT.jar" ]; then
    echo ""
    echo "Services not built yet. Building all services..."
    ./build-all-services.sh
fi

# Create logs directory
mkdir -p logs

# Start Customer Service (Port 8081)
echo ""
echo "Starting Customer Service on port 8081..."
cd customer-service
nohup mvn spring-boot:run > ../logs/customer-service.log 2>&1 &
CUSTOMER_PID=$!
echo "Customer Service started with PID: $CUSTOMER_PID"
cd ..

# Wait a bit for Customer Service to start
sleep 5

# Start Inventory Service (Port 8082)
echo ""
echo "Starting Inventory Service on port 8082..."
cd inventory-service
nohup mvn spring-boot:run > ../logs/inventory-service.log 2>&1 &
INVENTORY_PID=$!
echo "Inventory Service started with PID: $INVENTORY_PID"
cd ..

# Wait a bit for Inventory Service to start
sleep 5

# Start Order Service (Port 8083)
echo ""
echo "Starting Order Service on port 8083..."
cd order-service
nohup mvn spring-boot:run > ../logs/order-service.log 2>&1 &
ORDER_PID=$!
echo "Order Service started with PID: $ORDER_PID"
cd ..

# Save PIDs to file for shutdown script
echo "$CUSTOMER_PID" > .service-pids
echo "$INVENTORY_PID" >> .service-pids
echo "$ORDER_PID" >> .service-pids

echo ""
echo "============================="
echo "All services are starting up!"
echo ""
echo "Service URLs:"
echo "  - Customer Service:  http://localhost:8081/api/customers"
echo "  - Inventory Service: http://localhost:8082/api/products"
echo "  - Order Service:     http://localhost:8083/api/orders"
echo ""
echo "Logs are available in:"
echo "  - logs/customer-service.log"
echo "  - logs/inventory-service.log"
echo "  - logs/order-service.log"
echo ""
echo "To stop all services, run: ./stop-all-services.sh"
echo ""
echo "Waiting for services to be ready (this may take 15-30 seconds)..."
sleep 10

# Check if services are responding
echo ""
echo "Checking service health..."

# Check Customer Service
if curl -s http://localhost:8081/api/customers > /dev/null 2>&1; then
    echo "✓ Customer Service is ready"
else
    echo "⚠ Customer Service is still starting..."
fi

# Check Inventory Service
if curl -s http://localhost:8082/api/products > /dev/null 2>&1; then
    echo "✓ Inventory Service is ready"
else
    echo "⚠ Inventory Service is still starting..."
fi

# Check Order Service
if curl -s http://localhost:8083/api/orders > /dev/null 2>&1; then
    echo "✓ Order Service is ready"
else
    echo "⚠ Order Service is still starting..."
fi

echo ""
echo "If a service is still starting, wait a few more seconds and check the logs."
