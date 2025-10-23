#!/bin/bash
set -e

echo "Building all microservices..."
echo "=============================="

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

# Build Customer Service
echo ""
echo "Building Customer Service..."
echo "----------------------------"
cd customer-service
mvn clean install
cd ..

# Build Inventory Service
echo ""
echo "Building Inventory Service..."
echo "-----------------------------"
cd inventory-service
mvn clean install
cd ..

# Build Order Service
echo ""
echo "Building Order Service..."
echo "-------------------------"
cd order-service
mvn clean install
cd ..

echo ""
echo "=============================="
echo "All services built successfully!"
echo ""
echo "JAR files created:"
echo "  - customer-service/target/customer-service-1.0.0-SNAPSHOT.jar"
echo "  - inventory-service/target/inventory-service-1.0.0-SNAPSHOT.jar"
echo "  - order-service/target/order-service-1.0.0-SNAPSHOT.jar"
echo ""
echo "To run all services, execute: ./start-all-services.sh"
