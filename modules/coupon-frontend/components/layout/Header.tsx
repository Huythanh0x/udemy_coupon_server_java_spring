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
            aria-label="GitHub Repository"
            className="inline-flex items-center gap-2 px-4 py-2 rounded-md bg-primary text-white font-medium shadow hover:bg-primary/90 transition-colors focus:outline-none focus:ring-2 focus:ring-primary/50"
          >
            <Github className="w-5 h-5" />
            <span>GitHub</span>
          </a>
        </div>
      </div>
    </header>
  )
}
