import Link from 'next/link'
import Image from 'next/image'

interface CouponCardProps {
  course: {
    courseId: number
    title: string
    previewImage: string
    category: string
    rating: number
    students: number
    contentLength: number
    couponUrl: string
    expiredDate: string
    usesRemaining: number
  }
}

export function CouponCard({ course }: CouponCardProps) {
  return (
    <Link href={`/coupons/${course.courseId}`}>
      <div className="bg-card border rounded-lg overflow-hidden hover:shadow-lg transition-shadow">
        <div className="relative h-48 w-full">
          <Image
            src={course.previewImage}
            alt={course.title}
            fill
            className="object-cover"
          />
        </div>
        <div className="p-4">
          <h3 className="font-semibold mb-2 line-clamp-2">{course.title}</h3>
          <div className="flex items-center gap-4 text-sm text-muted-foreground mb-2">
            <span>⭐ {course.rating.toFixed(1)}</span>
            <span>👥 {course.students.toLocaleString()}</span>
            <span>⏱️ {course.contentLength} min</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-xs bg-primary/10 text-primary px-2 py-1 rounded">
              {course.category}
            </span>
            <span className="text-xs text-muted-foreground">
              {course.usesRemaining} left
            </span>
          </div>
        </div>
      </div>
    </Link>
  )
}

