#!/bin/bash

# Run tests for all microservices
echo "========================================="
echo "Running Tests for All Microservices"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Track overall success
OVERALL_SUCCESS=true

# Test Customer Service
echo ""
echo "Testing Customer Service..."
cd customer-service
if mvn test; then
    echo -e "${GREEN}✓ Customer Service tests passed${NC}"
else
    echo -e "${RED}✗ Customer Service tests failed${NC}"
    OVERALL_SUCCESS=false
fi
cd ..

# Test Inventory Service
echo ""
echo "Testing Inventory Service..."
cd inventory-service
if mvn test; then
    echo -e "${GREEN}✓ Inventory Service tests passed${NC}"
else
    echo -e "${RED}✗ Inventory Service tests failed${NC}"
    OVERALL_SUCCESS=false
fi
cd ..

# Test Order Service
echo ""
echo "Testing Order Service..."
cd order-service
if mvn test; then
    echo -e "${GREEN}✓ Order Service tests passed${NC}"
else
    echo -e "${RED}✗ Order Service tests failed${NC}"
    OVERALL_SUCCESS=false
fi
cd ..

echo ""
echo "========================================="
if [ "$OVERALL_SUCCESS" = true ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed. Please review the output above.${NC}"
    exit 1
fi
