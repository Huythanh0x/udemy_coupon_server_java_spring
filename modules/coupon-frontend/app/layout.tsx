import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'
import { Header } from '@/components/layout/Header'
import { Footer } from '@/components/layout/Footer'
import { Providers } from '@/lib/providers'
import { ThemeProvider } from '@/lib/theme-provider'
import { ThemeToggle } from '@/components/ui/ThemeToggle'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: '100% Off Udemy Coupons - Free Online Courses',
  description: 'Discover 100% off Udemy coupons for free online courses. Browse thousands of premium courses in development, business, design, and more for free.',
  keywords: ['udemy coupons', 'free courses', 'online learning', 'udemy discount', '100% off'],
  openGraph: {
    title: '100% Off Udemy Coupons',
    description: 'Discover 100% off Udemy coupons for free online courses',
    type: 'website',
  },
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={inter.className}>
        <ThemeProvider>
          <Providers>
            <Header />
            <main className="min-h-screen">
              {children}
            </main>
            <Footer />
            <ThemeToggle />
          </Providers>
        </ThemeProvider>
      </body>
    </html>
  )
}

