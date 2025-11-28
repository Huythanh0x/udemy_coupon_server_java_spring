import Link from 'next/link'

export function Hero() {
  return (
    <section className="bg-gradient-to-br from-primary/10 to-primary/5 py-20">
      <div className="container mx-auto px-4">
        <div className="max-w-3xl mx-auto text-center">
          <h1 className="text-5xl font-bold mb-6">
            100% Off Udemy Coupons
          </h1>
          <p className="text-xl text-muted-foreground mb-8">
            Discover thousands of free online courses. Get premium Udemy courses
            completely free with our validated 100% off coupons.
          </p>
          <div className="flex gap-4 justify-center">
            <Link
              href="/coupons"
              className="bg-primary text-primary-foreground px-8 py-3 rounded-lg font-semibold hover:bg-primary/90 transition-colors"
            >
              Browse Free Courses
            </Link>
            <Link
              href="/about"
              className="border border-primary text-primary px-8 py-3 rounded-lg font-semibold hover:bg-primary/10 transition-colors"
            >
              Learn More
            </Link>
          </div>
        </div>
      </div>
    </section>
  )
}

