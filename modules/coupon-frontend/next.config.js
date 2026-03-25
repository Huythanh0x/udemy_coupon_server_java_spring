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
    // In production we prefer same-origin relative calls (Cloudflare tunnel routes /api/v1).
    // Locally you can set NEXT_PUBLIC_API_URL via .env.local.
    NEXT_PUBLIC_API_URL:
      process.env.NEXT_PUBLIC_API_URL ||
      (process.env.NODE_ENV === 'production' ? '' : 'http://localhost:8080'),
  },
  // Output configuration (for static export if needed)
  // output: 'standalone', // Uncomment for Docker deployment
}

module.exports = nextConfig

