# Quick Start Guide

## 🚀 Getting Started

### 1. Install Dependencies

```bash
cd modules/coupon-frontend
npm install
```

### 2. Configure Environment

Create `.env.local` file:

```bash
cp .env.example .env.local
```

Edit `.env.local` and set your API URL:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### 3. Start Development Server

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

## 📁 Project Structure

```
coupon-frontend/
├── app/                          # Next.js App Router
│   ├── (marketing)/             # Marketing pages group
│   │   ├── page.tsx             # Landing page (/)
│   │   ├── about/               # About page (/about)
│   │   ├── terms/               # Terms page (/terms)
│   │   └── privacy/             # Privacy page (/privacy)
│   ├── coupons/                  # Coupon pages
│   │   ├── page.tsx             # Listing page (/coupons)
│   │   └── [courseId]/         # Detail page (/coupons/123)
│   ├── layout.tsx               # Root layout
│   └── globals.css              # Global styles
├── components/                   # React components
│   ├── ui/                      # Reusable UI components (to be added)
│   ├── coupons/                 # Coupon components
│   ├── layout/                  # Header, Footer
│   └── marketing/               # Marketing page components
├── lib/                          # Utilities
│   ├── api/                     # API client
│   ├── providers.tsx            # React Query provider
│   └── utils.ts                 # Helper functions
└── types/                        # TypeScript types
```

## 🎯 Next Steps

1. **Install dependencies**: `npm install`
2. **Start backend**: Ensure your Spring Boot API is running on port 8080
3. **Start frontend**: `npm run dev`
4. **Implement API integration**: Connect components to real API endpoints
5. **Add UI components**: Install shadcn/ui or similar component library
6. **Style components**: Complete the styling for all components

## 🔧 Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm start` - Start production server
- `npm run lint` - Run ESLint
- `npm run type-check` - Type check without emitting files

## 📝 Notes

- All components are currently placeholder implementations
- API integration needs to be completed
- React Query is set up but not yet used in components
- Tailwind CSS is configured and ready to use
- TypeScript types are defined in `types/coupon.ts`

## 🐛 Troubleshooting

**Port 3000 already in use?**
- Change port: `npm run dev -- -p 3001`

**API connection issues?**
- Check `.env.local` has correct `NEXT_PUBLIC_API_URL`
- Ensure backend is running and CORS is configured
- Check browser console for errors

**TypeScript errors?**
- Run `npm run type-check` to see all errors
- Ensure all dependencies are installed

