import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'About Us - 100% Off Udemy Coupons',
  description: 'Learn about our mission to provide free access to quality online courses through Udemy coupons.',
}

export default function AboutPage() {
  return (
    <div className="container mx-auto px-4 py-16">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-4xl font-bold mb-8">About Us</h1>
        <div className="prose prose-lg">
          <p className="text-lg text-muted-foreground mb-6">
            We are dedicated to making quality education accessible to everyone by providing
            100% off Udemy coupons for free online courses.
          </p>
          <h2 className="text-2xl font-semibold mt-8 mb-4">Our Mission</h2>
          <p className="mb-4">
            Our mission is to democratize education by helping learners access premium courses
            without financial barriers. We curate and validate the best free Udemy coupons
            available online.
          </p>
          <h2 className="text-2xl font-semibold mt-8 mb-4">How It Works</h2>
          <p className="mb-4">
            We continuously crawl and validate coupons from trusted sources, ensuring that
            all listed courses are genuinely free and available. Our automated system
            checks coupon validity in real-time.
          </p>
        </div>
      </div>
    </div>
  )
}

