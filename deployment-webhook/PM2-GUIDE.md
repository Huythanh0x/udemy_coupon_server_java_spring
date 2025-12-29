# PM2 Management Guide for Webhook Server

## Install PM2

```bash
npm install -g pm2
```

## Start Webhook Server

```bash
cd /opt/training-coupon/deployment-webhook
pm2 start server.js --name deployment-webhook
```

## Basic Commands

```bash
# Check status
pm2 status

# View logs (real-time)
pm2 logs deployment-webhook

# View last 100 lines
pm2 logs deployment-webhook --lines 100

# Restart
pm2 restart deployment-webhook

# Stop
pm2 stop deployment-webhook

# Delete from PM2
pm2 delete deployment-webhook

# Reload (zero-downtime)
pm2 reload deployment-webhook
```

## Auto-start on Boot

```bash
# Save current PM2 process list
pm2 save

# Generate startup script
pm2 startup

# Copy and run the command it outputs (usually something like):
# sudo env PATH=$PATH:/usr/bin pm2 startup systemd -u root --hp /root
```

## Monitoring

```bash
# Monitor (CPU, memory, etc.)
pm2 monit

# Show detailed info
pm2 show deployment-webhook

# View all logs
pm2 logs
```

## Useful Commands

```bash
# Restart all PM2 processes
pm2 restart all

# Stop all
pm2 stop all

# Delete all
pm2 delete all

# Clear logs
pm2 flush

# Save current process list
pm2 save
```

