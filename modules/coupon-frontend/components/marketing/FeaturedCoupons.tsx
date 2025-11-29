'use client'

import Link from 'next/link'
import { useFeaturedCoupons } from '@/hooks/useCoupons'
import { CouponCard } from '@/components/coupons/CouponCard'
import { Loader2 } from 'lucide-react'

export function FeaturedCoupons() {
  const { data, isLoading, error } = useFeaturedCoupons()

  return (
    <section className="py-16 bg-muted/30">
      <div className="container mx-auto px-4">
        <div className="text-center mb-8">
          <h2 className="text-3xl font-bold mb-2">Featured Free Courses</h2>
          <p className="text-muted-foreground">Top 10 most popular courses by student enrollment</p>
        </div>
        
        {isLoading && (
          <div className="flex justify-center items-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-primary" />
          </div>
        )}

        {error && (
          <div className="text-center py-12 text-destructive">
            <p>Failed to load featured courses. Please try again later.</p>
          </div>
        )}

        {data && data.courses.length > 0 && (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5 gap-6 mb-8">
              {data.courses.map((course) => (
                <CouponCard key={course.courseId} course={course} />
              ))}
            </div>
            <div className="text-center">
              <Link
                href="/coupons"
                className="inline-flex items-center px-6 py-3 bg-primary text-primary-foreground rounded-lg font-semibold hover:bg-primary/90 transition-colors shadow-lg hover:shadow-xl"
              >
                Browse All Free Courses
              </Link>
            </div>
          </>
        )}

        {data && data.courses.length === 0 && (
          <div className="text-center py-12 text-muted-foreground">
            <p>No featured courses available at the moment.</p>
          </div>
        )}
      </div>
    </section>
  )
}

