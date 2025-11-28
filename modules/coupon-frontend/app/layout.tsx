import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'
import { Header } from '@/components/layout/Header'
import { Footer } from '@/components/layout/Footer'
import { Providers } from '@/lib/providers'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: '100% Off Udemy Coupons - Free Online Courses',
  description: 'Discover 100% off Udemy coupons for free online courses. Browse thousands of free courses in development, business, design, and more.',
  keywords: ['udemy coupons', 'free courses', 'online learning', 'udemy discount'],
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
    <html lang="en">
      <body className={inter.className}>
        <Providers>
          <Header />
          <main className="min-h-screen">
            {children}
          </main>
          <Footer />
        </Providers>
      </body>
    </html>
  )
}

