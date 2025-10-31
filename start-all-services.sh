#!/bin/bash

# Start all microservices in the correct order
echo "========================================"
echo "Starting All Microservices"
echo "========================================"

# Check if services are already running
if [ -f .service-pids ]; then
    echo "⚠️  Services may already be running. Run stop-all-services.sh first."
    exit 1
fi

# Array to store PIDs
declare -a PIDS

# Start Customer Service (Port 8081)
echo ""
echo "Starting Customer Service on port 8081..."
cd customer-service || exit 1
mvn spring-boot:run > ../logs/customer-service.log 2>&1 &
CUSTOMER_PID=$!
PIDS+=($CUSTOMER_PID)
echo "Customer Service started with PID: $CUSTOMER_PID"
cd ..

# Wait for Customer Service to be ready
echo "Waiting for Customer Service to start..."
sleep 15

# Start Inventory Service (Port 8082)
echo ""
echo "Starting Inventory Service on port 8082..."
cd inventory-service || exit 1
mvn spring-boot:run > ../logs/inventory-service.log 2>&1 &
INVENTORY_PID=$!
PIDS+=($INVENTORY_PID)
echo "Inventory Service started with PID: $INVENTORY_PID"
cd ..

# Wait for Inventory Service to be ready
echo "Waiting for Inventory Service to start..."
sleep 15

# Start Order Service (Port 8083)
echo ""
echo "Starting Order Service on port 8083..."
cd order-service || exit 1
mvn spring-boot:run > ../logs/order-service.log 2>&1 &
ORDER_PID=$!
PIDS+=($ORDER_PID)
echo "Order Service started with PID: $ORDER_PID"
cd ..

# Wait for Order Service to be ready
echo "Waiting for Order Service to start..."
sleep 15

# Save PIDs to file
echo "${PIDS[@]}" > .service-pids

echo ""
echo "========================================"
echo "All Services Started"
echo "========================================"
echo "Customer Service:  http://localhost:8081"
echo "Inventory Service: http://localhost:8082"
echo "Order Service:     http://localhost:8083"
echo ""
echo "View logs:"
echo "  tail -f logs/customer-service.log"
echo "  tail -f logs/inventory-service.log"
echo "  tail -f logs/order-service.log"
echo ""
echo "To stop all services, run:"
echo "  ./stop-all-services.sh"
