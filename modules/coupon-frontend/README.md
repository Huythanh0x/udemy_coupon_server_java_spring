# Coupon Frontend

Next.js frontend for the Udemy Coupon Platform.

## Tech Stack

- **Next.js 14+** (App Router)
- **TypeScript**
- **Tailwind CSS**
- **React Query** (TanStack Query)
- **Axios**

## Getting Started

### Prerequisites

- Node.js 18+ and npm 9+
- Backend API running on `http://localhost:8080` (or configure via `.env.local`)

### Installation

```bash
# Install dependencies
npm install

# Copy environment variables
cp .env.example .env.local

# Edit .env.local with your API URL if needed
```

### Development

```bash
# Start development server
npm run dev

# Open http://localhost:3000
```

### Build

```bash
# Build for production
npm run build

# Start production server
npm start
```

### Linting

```bash
# Run ESLint
npm run lint

# Type check
npm run type-check
```

## Project Structure

```
coupon-frontend/
├── app/                    # Next.js App Router
│   ├── (marketing)/       # Marketing pages (landing, about, terms, privacy)
│   ├── coupons/           # Coupon listing and detail pages
│   ├── layout.tsx         # Root layout
│   └── globals.css        # Global styles
├── components/            # React components
│   ├── ui/                # Reusable UI components
│   ├── coupons/           # Coupon-related components
│   ├── layout/            # Layout components (Header, Footer)
│   └── marketing/         # Marketing page components
├── lib/                   # Utilities and API client
│   ├── api/              # API client functions
│   └── utils/            # Helper functions
├── hooks/                 # Custom React hooks
├── types/                 # TypeScript type definitions
└── public/               # Static assets
```

## Environment Variables

Create a `.env.local` file:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## API Integration

The frontend communicates with the Spring Boot backend API:

- `GET /api/v1/coupons` - List coupons with filters
- `GET /api/v1/coupons/{courseId}` - Get coupon details

See `lib/api/client.ts` for API client implementation.

## Features

- ✅ Landing page
- ✅ Coupon listing with filters
- ✅ Coupon detail pages
- ✅ Terms & Privacy pages
- ✅ About page
- ✅ Responsive design
- ✅ SEO optimization
- 🚧 Advanced filtering (in progress)
- 🚧 Search functionality (in progress)
- 🚧 Pagination (in progress)

## Development Notes

- Components marked with `'use client'` are client components
- Server components are used by default in App Router
- API calls should use React Query for caching and state management
- Use TypeScript types from `types/coupon.ts` for type safety

## Next Steps

1. Implement React Query hooks for data fetching
2. Complete coupon listing with real API integration
3. Add pagination
4. Implement advanced filtering
5. Add loading and error states
6. Optimize images and performance
7. Add analytics (Google Analytics)
8. Implement affiliate link tracking

