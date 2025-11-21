#!/bin/bash

# Run all microservices in separate terminal windows
echo "========================================="
echo "Starting All Microservices"
echo "========================================="

# Force Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo "Using Java 17: $JAVA_HOME"
echo ""
echo "Services will start in the following order:"
echo "  1. Customer Service (Port 8081)"
echo "  2. Inventory Service (Port 8082)"
echo "  3. Order Service (Port 8083)"
echo ""
echo "Press Ctrl+C in each terminal window to stop the services"
echo ""

# Detect OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    echo "Starting services on macOS..."
    
    # Start Customer Service
    osascript -e 'tell app "Terminal" to do script "cd \"'$(pwd)'/customer-service\" && echo \"Starting Customer Service on port 8081...\" && mvn spring-boot:run"'
    
    # Wait a bit for Customer Service to start
    sleep 5
    
    # Start Inventory Service
    osascript -e 'tell app "Terminal" to do script "cd \"'$(pwd)'/inventory-service\" && echo \"Starting Inventory Service on port 8082...\" && mvn spring-boot:run"'
    
    # Wait a bit for Inventory Service to start
    sleep 5
    
    # Start Order Service
    osascript -e 'tell app "Terminal" to do script "cd \"'$(pwd)'/order-service\" && echo \"Starting Order Service on port 8083...\" && mvn spring-boot:run"'
    
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    echo "Starting services on Linux..."
    
    # Try gnome-terminal first, then xterm
    if command -v gnome-terminal &> /dev/null; then
        gnome-terminal -- bash -c "cd customer-service && echo 'Starting Customer Service on port 8081...' && mvn spring-boot:run; exec bash"
        sleep 5
        gnome-terminal -- bash -c "cd inventory-service && echo 'Starting Inventory Service on port 8082...' && mvn spring-boot:run; exec bash"
        sleep 5
        gnome-terminal -- bash -c "cd order-service && echo 'Starting Order Service on port 8083...' && mvn spring-boot:run; exec bash"
    elif command -v xterm &> /dev/null; then
        xterm -e "cd customer-service && echo 'Starting Customer Service on port 8081...' && mvn spring-boot:run; exec bash" &
        sleep 5
        xterm -e "cd inventory-service && echo 'Starting Inventory Service on port 8082...' && mvn spring-boot:run; exec bash" &
        sleep 5
        xterm -e "cd order-service && echo 'Starting Order Service on port 8083...' && mvn spring-boot:run; exec bash" &
    else
        echo "No supported terminal emulator found. Please install gnome-terminal or xterm."
        echo ""
        echo "Alternatively, run services manually in separate terminals:"
        echo "  Terminal 1: cd customer-service && mvn spring-boot:run"
        echo "  Terminal 2: cd inventory-service && mvn spring-boot:run"
        echo "  Terminal 3: cd order-service && mvn spring-boot:run"
        exit 1
    fi
    
elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    # Windows (Git Bash or similar)
    echo "Starting services on Windows..."
    
    start "Customer Service" cmd /k "cd customer-service && echo Starting Customer Service on port 8081... && mvn spring-boot:run"
    sleep 5
    start "Inventory Service" cmd /k "cd inventory-service && echo Starting Inventory Service on port 8082... && mvn spring-boot:run"
    sleep 5
    start "Order Service" cmd /k "cd order-service && echo Starting Order Service on port 8083... && mvn spring-boot:run"
    
else
    echo "Unsupported operating system: $OSTYPE"
    echo ""
    echo "Please run services manually in separate terminals:"
    echo "  Terminal 1: cd customer-service && mvn spring-boot:run"
    echo "  Terminal 2: cd inventory-service && mvn spring-boot:run"
    echo "  Terminal 3: cd order-service && mvn spring-boot:run"
    exit 1
fi

echo ""
echo "Services are starting..."
echo ""
echo "Service URLs:"
echo "  Customer Service:  http://localhost:8081"
echo "  Inventory Service: http://localhost:8082"
echo "  Order Service:     http://localhost:8083"
echo ""
echo "H2 Console URLs:"
echo "  Customer DB:  http://localhost:8081/h2-console"
echo "  Inventory DB: http://localhost:8082/h2-console"
echo "  Order DB:     http://localhost:8083/h2-console"
