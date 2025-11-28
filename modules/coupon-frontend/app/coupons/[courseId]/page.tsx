import { CouponDetail } from '@/components/coupons/CouponDetail'
import { notFound } from 'next/navigation'

interface PageProps {
  params: {
    courseId: string
  }
}

export default async function CouponDetailPage({ params }: PageProps) {
  const { courseId } = params

  if (!courseId) {
    notFound()
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <CouponDetail courseId={courseId} />
    </div>
  )
}

