/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  swcMinify: true,
  images: {
    domains: [
      'img-c.udemycdn.com',
      'udemy-images.udemy.com',
      'www.udemy.com'
    ],
    remotePatterns: [
      {
        protocol: 'https',
        hostname: '**.udemycdn.com',
      },
      {
        protocol: 'https',
        hostname: '**.udemy.com',
      },
    ],
  },
  // Environment variables
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  },
  // Output configuration (for static export if needed)
  // output: 'standalone', // Uncomment for Docker deployment
}

module.exports = nextConfig

