#!/bin/bash

# Load environment variables from .env (or file specified via ENV_FILE)
ENV_FILE="${ENV_FILE:-.env}"
if [ -f "$ENV_FILE" ]; then
    echo "🔐 Loading environment variables from $ENV_FILE"
    set -a
    # shellcheck disable=SC1090
    source "$ENV_FILE"
    set +a
else
    echo "ℹ️  $ENV_FILE not found. Using default inline configuration values."
fi

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to get process info using a port
get_port_process() {
    local port=$1
    local pid=$(lsof -ti :$port -sTCP:LISTEN 2>/dev/null | head -1)
    if [ -n "$pid" ]; then
        local cmd=$(ps -p $pid -o command= 2>/dev/null | head -c 100)
        echo "  PID: $pid"
        echo "  Command: $cmd"
    fi
}

# Check if ports are available
PORTS_IN_USE=false

if check_port 8080; then
    echo "❌ Port 8080 is already in use!"
    echo "Process info:"
    get_port_process 8080 | sed 's/^p/  PID: /;s/^c/  Command: /;s/^n/  Name: /'
    PORTS_IN_USE=true
fi

if check_port 8081; then
    echo "❌ Port 8081 is already in use!"
    echo "Process info:"
    get_port_process 8081 | sed 's/^p/  PID: /;s/^c/  Command: /;s/^n/  Name: /'
    PORTS_IN_USE=true
fi

if [ "$PORTS_IN_USE" = true ]; then
    echo ""
    echo "⚠️  Please stop the existing processes on these ports before starting."
    echo ""
    read -p "Do you want to kill the processes on these ports? (y/N): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Stopping processes..."
        if check_port 8080; then
            lsof -ti :8080 -sTCP:LISTEN | xargs kill -9 2>/dev/null
            sleep 1
            echo "  ✓ Freed port 8080"
        fi
        if check_port 8081; then
            lsof -ti :8081 -sTCP:LISTEN | xargs kill -9 2>/dev/null
            sleep 1
            echo "  ✓ Freed port 8081"
        fi
        echo ""
    else
        echo "Exiting. Please free the ports manually and try again."
        exit 1
    fi
fi

echo "Starting services with local profile..."
echo ""
echo "📡 API Service: http://localhost:8080"
echo "🕷️  Crawler Service: http://localhost:8081"
echo "⚠️  Press Ctrl+C to stop all services"
echo ""

./gradlew :modules:coupon-api-service:bootRun --args='--spring.profiles.active=local' --no-daemon &
API_PID=$!
echo "Starting API service... (PID: $API_PID)"

./gradlew :modules:coupon-crawler-service:bootRun --args='--spring.profiles.active=local' --no-daemon &
CRAWLER_PID=$!
echo "Starting Crawler service... (PID: $CRAWLER_PID)"

check_health() {
    local port=$1
    curl -s -o /dev/null -w "%{http_code}" --connect-timeout 2 "http://localhost:$port/actuator/health" > /dev/null 2>&1
    return $?
}

API_READY=false
CRAWLER_READY=false

while [ "$API_READY" = false ] || [ "$CRAWLER_READY" = false ]; do
    if [ "$API_READY" = false ] && check_health 8080; then
        API_READY=true
        echo "✓ API service is up"
    fi
    if [ "$CRAWLER_READY" = false ] && check_health 8081; then
        CRAWLER_READY=true
        echo "✓ Crawler service is up"
    fi
    if [ "$API_READY" = true ] && [ "$CRAWLER_READY" = true ]; then
        echo "✅ Both services are ready!"
        echo "📡 API Service: http://localhost:8080"
        echo "🕷️  Crawler Service: http://localhost:8081"
        echo ""
    fi
    sleep 1
done


# Cleanup on exit
trap "echo ''; echo '🛑 Shutting down services...'; kill $API_PID $CRAWLER_PID 2>/dev/null; exit" INT TERM

# Wait for API process
wait $API_PID

