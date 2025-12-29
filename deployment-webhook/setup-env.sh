#!/bin/bash

# Setup script for deployment webhook environment variables
# Run this script on your server to create the .env file

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"
ENV_EXAMPLE="${SCRIPT_DIR}/env.example"

if [ -f "$ENV_FILE" ]; then
    echo "Warning: .env file already exists at $ENV_FILE"
    echo "Backing up to .env.backup"
    cp "$ENV_FILE" "${ENV_FILE}.backup"
fi

# Check if secret key is provided as argument
if [ -z "$1" ]; then
    echo "Usage: $0 <webhook-secret-key> [deployment-path]"
    echo ""
    echo "Example:"
    echo "  $0 '2u1ug0bUr4KYOGNfCUm'"
    echo "  $0 '2u1ug0bUr4KYOGNfCUm' '/opt/training-coupon'"
    exit 1
fi

WEBHOOK_SECRET_KEY="$1"
DEPLOYMENT_PATH="${2:-/opt/training-coupon}"

# Create .env file
cat > "$ENV_FILE" << EOF
# Webhook Configuration
WEBHOOK_SECRET_KEY=${WEBHOOK_SECRET_KEY}
WEBHOOK_PORT=3000

# Deployment Configuration
DEPLOYMENT_PATH=${DEPLOYMENT_PATH}

# Docker Hub Credentials (optional, for private repos)
# DOCKER_HUB_USERNAME=your-dockerhub-username
# DOCKER_HUB_TOKEN=your-dockerhub-token

# Node Environment
NODE_ENV=production
EOF

echo "✅ Created .env file at $ENV_FILE"
echo ""
echo "Configuration:"
echo "  WEBHOOK_SECRET_KEY: ${WEBHOOK_SECRET_KEY:0:10}..."
echo "  DEPLOYMENT_PATH: $DEPLOYMENT_PATH"
echo "  WEBHOOK_PORT: 3000"
echo ""
echo "⚠️  Remember to restart the webhook server for changes to take effect:"
echo "   pm2 restart deployment-webhook"
echo "   # or"
echo "   systemctl restart deployment-webhook"


