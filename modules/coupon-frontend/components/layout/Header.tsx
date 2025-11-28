import Link from 'next/link'

export function Header() {
  return (
    <header className="border-b">
      <div className="container mx-auto px-4 py-4">
        <div className="flex items-center justify-between">
          <Link href="/" className="text-2xl font-bold text-primary">
            Udemy Coupons
          </Link>
          <nav className="flex gap-6">
            <Link href="/coupons" className="hover:text-primary transition-colors">
              Browse Courses
            </Link>
            <Link href="/about" className="hover:text-primary transition-colors">
              About
            </Link>
          </nav>
        </div>
      </div>
    </header>
  )
}

