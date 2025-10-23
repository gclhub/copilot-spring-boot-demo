#!/bin/bash

echo "Stopping all microservices..."
echo "============================="

if [ ! -f ".service-pids" ]; then
    echo "No running services found (no .service-pids file)"
    echo ""
    echo "Attempting to kill any services on ports 8081, 8082, 8083..."
    
    # Try to kill by port
    for port in 8081 8082 8083; do
        PID=$(lsof -ti :$port 2>/dev/null || echo "")
        if [ -n "$PID" ]; then
            echo "Killing process on port $port (PID: $PID)"
            kill $PID 2>/dev/null || true
        fi
    done
    
    exit 0
fi

# Read PIDs from file and kill them
while read PID; do
    if [ -n "$PID" ]; then
        if ps -p $PID > /dev/null 2>&1; then
            echo "Stopping service with PID: $PID"
            kill $PID 2>/dev/null || true
        else
            echo "Service with PID $PID is not running"
        fi
    fi
done < .service-pids

# Wait a bit for graceful shutdown
sleep 3

# Force kill any remaining processes on the ports
echo ""
echo "Checking for any remaining processes..."
for port in 8081 8082 8083; do
    PID=$(lsof -ti :$port 2>/dev/null || echo "")
    if [ -n "$PID" ]; then
        echo "Force killing process on port $port (PID: $PID)"
        kill -9 $PID 2>/dev/null || true
    fi
done

# Remove PID file
rm -f .service-pids

echo ""
echo "============================="
echo "All services stopped!"
