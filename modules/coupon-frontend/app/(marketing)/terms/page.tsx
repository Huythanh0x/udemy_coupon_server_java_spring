import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Terms of Service - 100% Off Udemy Coupons',
  description: 'Terms of Service for the Udemy Coupon Platform.',
}

export default function TermsPage() {
  return (
    <div className="container mx-auto px-4 py-16">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-4xl font-bold mb-8">Terms of Service</h1>
        <div className="prose prose-lg">
          <p className="text-sm text-muted-foreground mb-8">
            Last updated: {new Date().toLocaleDateString()}
          </p>
          <section className="mb-8">
            <h2 className="text-2xl font-semibold mb-4">1. Acceptance of Terms</h2>
            <p className="mb-4">
              By accessing and using this website, you accept and agree to be bound by the
              terms and provision of this agreement.
            </p>
          </section>
          <section className="mb-8">
            <h2 className="text-2xl font-semibold mb-4">2. Use License</h2>
            <p className="mb-4">
              Permission is granted to temporarily access the materials on this website
              for personal, non-commercial transitory viewing only.
            </p>
          </section>
          <section className="mb-8">
            <h2 className="text-2xl font-semibold mb-4">3. Disclaimer</h2>
            <p className="mb-4">
              The materials on this website are provided on an 'as is' basis. We make no
              warranties, expressed or implied, and hereby disclaim all warranties including
              implied warranties of merchantability or fitness for a particular purpose.
            </p>
          </section>
        </div>
      </div>
    </div>
  )
}

