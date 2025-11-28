import Link from 'next/link'
import { Github } from 'lucide-react'

export function Header() {
  return (
    <header className="border-b">
      <div className="container mx-auto px-4 py-4">
        <div className="flex items-center justify-between">
          <Link href="/" className="text-2xl font-bold text-primary">
            Udemy Coupons
          </Link>
          <a
            href="https://github.com/Huythanh0x/udemy_coupon_server_java_spring"
            target="_blank"
            rel="noopener noreferrer"
            className="hover:text-primary transition-colors"
            aria-label="GitHub Repository"
          >
            <Github className="w-6 h-6" />
          </a>
        </div>
      </div>
    </header>
  )
}

