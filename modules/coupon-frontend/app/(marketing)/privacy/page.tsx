import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Privacy Policy - 100% Off Udemy Coupons',
  description: 'Privacy Policy for the Udemy Coupon Platform.',
}

export default function PrivacyPage() {
  return (
    <div className="container mx-auto px-4 py-16">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-4xl font-bold mb-8">Privacy Policy</h1>
        <div className="prose prose-lg">
          <p className="text-sm text-muted-foreground mb-8">
            Last updated: {new Date().toLocaleDateString()}
          </p>
          <section className="mb-8">
            <h2 className="text-2xl font-semibold mb-4">1. Information We Collect</h2>
            <p className="mb-4">
              We collect information that you provide directly to us, such as when you
              use our services or contact us for support.
            </p>
          </section>
          <section className="mb-8">
            <h2 className="text-2xl font-semibold mb-4">2. How We Use Your Information</h2>
            <p className="mb-4">
              We use the information we collect to provide, maintain, and improve our
              services, and to communicate with you about our services.
            </p>
          </section>
          <section className="mb-8">
            <h2 className="text-2xl font-semibold mb-4">3. Cookies</h2>
            <p className="mb-4">
              We use cookies and similar tracking technologies to track activity on our
              website and hold certain information.
            </p>
          </section>
          <section className="mb-8">
            <h2 className="text-2xl font-semibold mb-4">4. Your Rights</h2>
            <p className="mb-4">
              You have the right to access, update, or delete your personal information
              at any time. Please contact us if you wish to exercise these rights.
            </p>
          </section>
        </div>
      </div>
    </div>
  )
}

