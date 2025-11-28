'use client'

interface CouponDetailProps {
  courseId: string
}

export function CouponDetail({ courseId }: CouponDetailProps) {
  // TODO: Fetch course details from API
  return (
    <div>
      <p>Course detail for ID: {courseId}</p>
      <p className="text-muted-foreground">Loading course details...</p>
    </div>
  )
}

