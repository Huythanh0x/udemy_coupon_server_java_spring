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

    // Step 2: Login to Docker Hub (if needed)
    results.steps.push({ step: 'docker_login', status: 'started' });
    if (process.env.DOCKER_HUB_TOKEN && process.env.DOCKER_HUB_USERNAME) {
      try {
        const loginCmd = `echo "${process.env.DOCKER_HUB_TOKEN}" | docker login -u "${process.env.DOCKER_HUB_USERNAME}" --password-stdin`;
        await execAsync(loginCmd);
        results.steps.push({ step: 'docker_login', status: 'completed' });
      } catch (error) {
        console.warn('Docker login failed, continuing anyway:', error.message);
        results.steps.push({ step: 'docker_login', status: 'skipped', reason: error.message });
      }
    } else {
      results.steps.push({ step: 'docker_login', status: 'skipped', reason: 'No credentials provided' });
    }

    // Step 3: Pull Docker images
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

    // Step 4: Stop existing services
    results.steps.push({ step: 'stop_services', status: 'started' });
    try {
      await execAsync('docker-compose -f docker-compose.prod.yml down || true');
      results.steps.push({ step: 'stop_services', status: 'completed' });
    } catch (error) {
      results.steps.push({ step: 'stop_services', status: 'warning', error: error.message });
    }

    // Step 5: Start services with new images
    results.steps.push({ step: 'start_services', status: 'started' });
    try {
      const envVars = {
        BASE_IMAGE_NAME: baseImageName,
        API_IMAGE_TAG: apiImageTag,
        CRAWLER_IMAGE_TAG: crawlerImageTag,
        ...process.env
      };

      const envString = Object.entries(envVars)
        .map(([key, value]) => `${key}=${value}`)
        .join(' ');

      const startCmd = `${envString} docker-compose -f docker-compose.prod.yml up -d`;
      const { stdout, stderr } = await execAsync(startCmd);
      
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

    // Step 6: Health check
    results.steps.push({ step: 'health_check', status: 'started' });
    await new Promise(resolve => setTimeout(resolve, 10000)); // Wait 10 seconds
    
    try {
      const { stdout } = await execAsync('docker-compose -f docker-compose.prod.yml ps');
      results.steps.push({ 
        step: 'health_check', 
        status: 'completed',
        services: stdout 
      });
    } catch (error) {
      results.steps.push({ step: 'health_check', status: 'warning', error: error.message });
    }

    // Step 7: Clean up old images
    results.steps.push({ step: 'cleanup', status: 'started' });
    try {
      await execAsync('docker image prune -f');
      results.steps.push({ step: 'cleanup', status: 'completed' });
    } catch (error) {
      results.steps.push({ step: 'cleanup', status: 'warning', error: error.message });
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

