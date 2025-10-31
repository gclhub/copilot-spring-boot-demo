#!/bin/bash

# Build all microservices
echo "========================================"
echo "Building All Microservices"
echo "========================================"

SERVICES=("customer-service" "inventory-service" "order-service")
FAILED=()

for service in "${SERVICES[@]}"; do
    echo ""
    echo "Building $service..."
    echo "----------------------------------------"
    
    cd "$service" || exit 1
    
    if mvn clean install -DskipTests; then
        echo "✅ $service built successfully"
    else
        echo "❌ $service build failed"
        FAILED+=("$service")
    fi
    
    cd ..
done

echo ""
echo "========================================"
echo "Build Summary"
echo "========================================"

if [ ${#FAILED[@]} -eq 0 ]; then
    echo "✅ All services built successfully!"
    exit 0
else
    echo "❌ The following services failed to build:"
    for service in "${FAILED[@]}"; do
        echo "   - $service"
    done
    exit 1
fi
