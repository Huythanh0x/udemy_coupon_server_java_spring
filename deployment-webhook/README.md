# Deployment Webhook Server

A simple Express.js webhook server that handles deployment requests from GitHub Actions.

## Features

- ✅ Secure webhook endpoint with secret key authentication
- ✅ Pulls Docker images from Docker Hub
- ✅ Stops and starts Docker Compose services
- ✅ Health checks after deployment
- ✅ Returns detailed deployment status to GitHub Actions
- ✅ Cleanup of old Docker images

## Setup

### 1. Install Dependencies

```bash
cd deployment-webhook
npm install
```

### 2. Configure Environment

```bash
cp .env.example .env
nano .env
```

**Required variables:**
- `WEBHOOK_SECRET_KEY` - Secret key for webhook authentication (generate a strong random string)
- `DEPLOYMENT_PATH` - Path where docker-compose.prod.yml is located (default: `/opt/training-coupon`)

**Optional variables:**
- `WEBHOOK_PORT` - Port to run webhook server (default: 3000)
- `DOCKER_HUB_USERNAME` - Docker Hub username (for private repos)
- `DOCKER_HUB_TOKEN` - Docker Hub access token (for private repos)

### 3. Generate Secret Key

```bash
# Generate a secure random key
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
```

Copy the output and use it as `WEBHOOK_SECRET_KEY`.

### 4. Run the Server

**Development:**
```bash
npm run dev
```

**Production (with PM2):**
```bash
npm install -g pm2
pm2 start server.js --name deployment-webhook
pm2 save
pm2 startup
```

**Production (with systemd):**
```bash
# Create systemd service file
sudo nano /etc/systemd/system/deployment-webhook.service
```

```ini
[Unit]
Description=Deployment Webhook Server
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/training-coupon/deployment-webhook
Environment=NODE_ENV=production
ExecStart=/usr/bin/node server.js
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable deployment-webhook
sudo systemctl start deployment-webhook
sudo systemctl status deployment-webhook
```

## Endpoints

### Health Check
```
GET /health
```

Returns server status.

### Deploy
```
POST /deploy
Headers:
  X-Webhook-Secret: <your-secret-key>
Body:
{
  "branch": "main",
  "apiImageTag": "api-main",
  "crawlerImageTag": "crawler-main",
  "baseImageName": "username/training-coupon-services"
}
```

## Security

- Always use HTTPS in production (use reverse proxy like Nginx)
- Use a strong, random secret key
- Keep the secret key secure (never commit to git)
- Consider IP whitelisting if possible
- Monitor webhook logs for unauthorized attempts

## Testing

```bash
# Test health endpoint
curl http://localhost:3000/health

# Test deployment endpoint
curl -X POST http://localhost:3000/deploy \
  -H "Content-Type: application/json" \
  -H "X-Webhook-Secret: your-secret-key" \
  -d '{
    "branch": "main",
    "apiImageTag": "api-main",
    "crawlerImageTag": "crawler-main",
    "baseImageName": "username/training-coupon-services"
  }'
```

## Troubleshooting

### Server won't start
- Check if port is already in use: `lsof -i :3000`
- Verify Node.js is installed: `node --version`
- Check environment variables are set correctly

### Deployment fails
- Check Docker is running: `docker ps`
- Verify docker-compose.prod.yml exists at DEPLOYMENT_PATH
- Check Docker Hub credentials if using private repo
- Review server logs for detailed error messages

### Permission denied
- Ensure user has Docker permissions: `usermod -aG docker $USER`
- Check file permissions in deployment directory

## Monitoring

Check server logs:
```bash
# PM2
pm2 logs deployment-webhook

# systemd
journalctl -u deployment-webhook -f

# Direct
# Logs are printed to stdout/stderr
```

