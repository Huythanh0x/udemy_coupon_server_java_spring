const express = require('express');
const { exec } = require('child_process');
const { promisify } = require('util');
const execAsync = promisify(exec);
const path = require('path');
require('dotenv').config();

const app = express();
const PORT = process.env.WEBHOOK_PORT || 3000;
const SECRET_KEY = process.env.WEBHOOK_SECRET_KEY;
const DEPLOYMENT_PATH = process.env.DEPLOYMENT_PATH || '/opt/training-coupon';

// Middleware
app.use(express.json());

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', message: 'Deployment webhook server is running' });
});

// Deployment webhook endpoint
app.post('/deploy', async (req, res) => {
  try {
    // Verify secret key
    const providedSecret = req.headers['x-webhook-secret'] || req.body.secret;
    
    if (!SECRET_KEY) {
      console.error('ERROR: WEBHOOK_SECRET_KEY not configured');
      return res.status(500).json({ 
        success: false, 
        error: 'Webhook server not properly configured' 
      });
    }

    if (providedSecret !== SECRET_KEY) {
      console.warn('Unauthorized deployment attempt');
      return res.status(401).json({ 
        success: false, 
        error: 'Invalid secret key' 
      });
    }

    console.log('Deployment request received');
    
    const { 
      branch, 
      apiImageTag, 
      crawlerImageTag, 
      baseImageName 
    } = req.body;

    // Validate required parameters
    if (!branch || !apiImageTag || !crawlerImageTag || !baseImageName) {
      return res.status(400).json({ 
        success: false, 
        error: 'Missing required parameters: branch, apiImageTag, crawlerImageTag, baseImageName' 
      });
    }

    // Start deployment process
    const deploymentResult = await deployServices({
      branch,
      apiImageTag,
      crawlerImageTag,
      baseImageName,
      deploymentPath: DEPLOYMENT_PATH
    });

    // Return result
    if (deploymentResult.success) {
      res.status(200).json({
        success: true,
        message: 'Deployment completed successfully',
        details: deploymentResult
      });
    } else {
      res.status(500).json({
        success: false,
        error: 'Deployment failed',
        details: deploymentResult
      });
    }

  } catch (error) {
    console.error('Deployment error:', error);
    res.status(500).json({
      success: false,
      error: error.message,
      stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
    });
  }
});

// Deployment function
async function deployServices({ branch, apiImageTag, crawlerImageTag, baseImageName, deploymentPath }) {
  const results = {
    success: false,
    steps: [],
    errors: []
  };

  try {
    // Step 1: Navigate to deployment directory
    results.steps.push({ step: 'navigate', status: 'started' });
    process.chdir(deploymentPath);
    results.steps.push({ step: 'navigate', status: 'completed', path: deploymentPath });

    // Step 2: Pull Docker images
    results.steps.push({ step: 'pull_images', status: 'started' });
    try {
      const pullApiCmd = `docker pull ${baseImageName}:${apiImageTag}`;
      const pullCrawlerCmd = `docker pull ${baseImageName}:${crawlerImageTag}`;
      
      await execAsync(pullApiCmd);
      results.steps.push({ step: 'pull_api_image', status: 'completed', image: `${baseImageName}:${apiImageTag}` });
      
      await execAsync(pullCrawlerCmd);
      results.steps.push({ step: 'pull_crawler_image', status: 'completed', image: `${baseImageName}:${crawlerImageTag}` });
      
      // Pull latest tags if on main branch
      if (branch === 'main') {
        try {
          await execAsync(`docker pull ${baseImageName}:api-latest || true`);
          await execAsync(`docker pull ${baseImageName}:crawler-latest || true`);
          results.steps.push({ step: 'pull_latest_tags', status: 'completed' });
        } catch (error) {
          results.steps.push({ step: 'pull_latest_tags', status: 'skipped', reason: error.message });
        }
      }
    } catch (error) {
      results.steps.push({ step: 'pull_images', status: 'failed', error: error.message });
      results.errors.push(`Failed to pull images: ${error.message}`);
      throw error;
    }

    // Step 3: Stop existing services
    results.steps.push({ step: 'stop_services', status: 'started' });
    try {
      await execAsync('docker compose -f docker-compose.prod.yml down || true');
      results.steps.push({ step: 'stop_services', status: 'completed' });
    } catch (error) {
      results.steps.push({ step: 'stop_services', status: 'warning', error: error.message });
    }

    // Step 4: Start services with new images
    results.steps.push({ step: 'start_services', status: 'started' });
    try {
      // Only include specific environment variables needed for docker compose
      // Filter out PM2 internal variables and non-string values
      const allowedEnvVars = [
        'BASE_IMAGE_NAME',
        'API_IMAGE_TAG',
        'CRAWLER_IMAGE_TAG',
        'SPRING_PROFILES_ACTIVE',
        'SPRING_DATASOURCE_PASSWORD',
        'SPRING_DATASOURCE_USERNAME',
        'SPRING_SECURITY_USER_NAME',
        'SPRING_SECURITY_USER_PASSWORD',
        'CUSTOM_JWT_SECRET',
        'CUSTOM_JWT_EXPIRATION',
        'SPRING_DATA_REDIS_PASSWORD',
        'PATH',
        'HOME',
        'USER'
      ];

      // Create a clean environment object with only necessary variables
      const cleanEnv = {
        BASE_IMAGE_NAME: baseImageName,
        API_IMAGE_TAG: apiImageTag,
        CRAWLER_IMAGE_TAG: crawlerImageTag,
      };

      // Add allowed environment variables from process.env (only strings, no PM2 vars)
      // Exclude PM2 variables that might have object values or special characters
      const pm2Patterns = /^(PM_|pm_|NODE_APP_INSTANCE|exec_mode|exec_interpreter|instances|name|filter_env|namespace|merge_logs|vizion|autostart|autorestart|watch|instance_var|pmx|automation|treekill|username|windowsHide|kill_retry_time|prev_restart_delay|axm_|vizion_|km_)/i;
      
      allowedEnvVars.forEach(key => {
        const value = process.env[key];
        if (value !== undefined && 
            typeof value === 'string' && 
            !pm2Patterns.test(key) &&
            !value.includes('[object Object]')) {
          cleanEnv[key] = value;
        }
      });

      // Always include PATH for docker/docker compose to work
      if (!cleanEnv.PATH) {
        cleanEnv.PATH = process.env.PATH || '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin';
      }

      const startCmd = 'docker compose -f docker-compose.prod.yml up -d';
      const { stdout, stderr } = await execAsync(startCmd, {
        env: cleanEnv
      });
      
      results.steps.push({ 
        step: 'start_services', 
        status: 'completed',
        output: stdout 
      });
    } catch (error) {
      results.steps.push({ step: 'start_services', status: 'failed', error: error.message });
      results.errors.push(`Failed to start services: ${error.message}`);
      throw error;
    }

    // Step 5: Health check
    results.steps.push({ step: 'health_check', status: 'started' });
    await new Promise(resolve => setTimeout(resolve, 10000)); // Wait 10 seconds
    
    try {
      const { stdout } = await execAsync('docker compose -f docker-compose.prod.yml ps');
      results.steps.push({ 
        step: 'health_check', 
        status: 'completed',
        services: stdout 
      });
    } catch (error) {
      results.steps.push({ step: 'health_check', status: 'warning', error: error.message });
    }

    results.success = true;
    results.message = 'Deployment completed successfully';
    
    return results;

  } catch (error) {
    results.success = false;
    results.errors.push(error.message);
    return results;
  }
}

// Start server
app.listen(PORT, () => {
  console.log(`Deployment webhook server running on port ${PORT}`);
  console.log(`Deployment path: ${DEPLOYMENT_PATH}`);
  console.log(`Health check: http://localhost:${PORT}/health`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('SIGTERM received, shutting down gracefully');
  process.exit(0);
});

process.on('SIGINT', () => {
  console.log('SIGINT received, shutting down gracefully');
  process.exit(0);
});

