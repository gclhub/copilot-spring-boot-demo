#!/bin/bash

# Stop all microservices
echo "========================================"
echo "Stopping All Microservices"
echo "========================================"

if [ ! -f .service-pids ]; then
    echo "⚠️  No PID file found. Services may not be running."
    echo "Attempting to find and kill Spring Boot processes for this project..."
    # More specific pattern to avoid killing unrelated processes
    pkill -f "copilot-spring-boot-demo.*spring-boot:run"
    exit 0
fi

# Read PIDs from file
PIDS=($(cat .service-pids))

for PID in "${PIDS[@]}"; do
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "Stopping process $PID..."
        kill "$PID"
        
        # Wait for process to stop
        for i in {1..10}; do
            if ! ps -p "$PID" > /dev/null 2>&1; then
                echo "✅ Process $PID stopped"
                break
            fi
            sleep 1
        done
        
        # Force kill if still running
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "⚠️  Force killing process $PID..."
            kill -9 "$PID"
        fi
    else
        echo "Process $PID not found (may have already stopped)"
    fi
done

# Clean up PID file
rm -f .service-pids

echo ""
echo "========================================"
echo "All Services Stopped"
echo "========================================"
