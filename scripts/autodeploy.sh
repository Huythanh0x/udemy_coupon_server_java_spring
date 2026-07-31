#!/bin/bash

# Configuration
PROJECT_DIR="/opt/training-coupon"
COMPOSE_FILE="docker-compose.prod.yml"

echo "--- $(date) ---"
echo "Checking for updates in $PROJECT_DIR..."

cd "$PROJECT_DIR" || { echo "Error: Could not enter $PROJECT_DIR"; exit 1; }

# Pull the latest images and capture output
PULL_OUTPUT=$(docker compose -f "$COMPOSE_FILE" pull 2>&1)
echo "$PULL_OUTPUT"

# Check if any new image was downloaded
if echo "$PULL_OUTPUT" | grep -q "Downloaded newer image"; then
    echo "New images detected. Restarting services..."
    docker compose -f "$COMPOSE_FILE" up -d

    echo "Cleaning up old images..."
    docker image prune -f

    echo "Deployment complete."
else
    echo "No updates found. Local images are up to date."
fi
