#!/bin/bash

# Build all microservices
echo "========================================="
echo "Building All Microservices"
echo "========================================="

# Force Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo "Using Java 17: $JAVA_HOME"
java -version

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Build Customer Service
echo ""
echo "Building Customer Service..."
cd customer-service
if mvn clean install -DskipTests; then
    echo -e "${GREEN}✓ Customer Service built successfully${NC}"
else
    echo -e "${RED}✗ Customer Service build failed${NC}"
    exit 1
fi
cd ..

# Build Inventory Service
echo ""
echo "Building Inventory Service..."
cd inventory-service
if mvn clean install -DskipTests; then
    echo -e "${GREEN}✓ Inventory Service built successfully${NC}"
else
    echo -e "${RED}✗ Inventory Service build failed${NC}"
    exit 1
fi
cd ..

# Build Order Service
echo ""
echo "Building Order Service..."
cd order-service
if mvn clean install -DskipTests; then
    echo -e "${GREEN}✓ Order Service built successfully${NC}"
else
    echo -e "${RED}✗ Order Service build failed${NC}"
    exit 1
fi
cd ..

echo ""
echo "========================================="
echo -e "${GREEN}All services built successfully!${NC}"
echo "========================================="
echo ""
echo "To run the services, execute: ./run-all.sh"
