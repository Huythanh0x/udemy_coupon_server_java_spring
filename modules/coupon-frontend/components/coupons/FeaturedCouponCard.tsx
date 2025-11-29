'use client'

import Link from 'next/link'
import Image from 'next/image'
import { Star, Users, Clock, Briefcase, Code, TrendingUp, Laptop, FileText, User, Palette, Megaphone, Heart, Camera, Dumbbell, Music, GraduationCap, Book } from 'lucide-react'
import type { CouponCourseData } from '@/types/coupon'
import { getCategoryColor, getCategoryIcon } from '@/lib/course-utils'
import { formatRelativeTime } from '@/lib/utils'

interface FeaturedCouponCardProps {
  course: CouponCourseData
}

const iconMap: Record<string, React.ComponentType<{ className?: string }>> = {
  'briefcase': Briefcase,
  'code': Code,
  'chart-line': TrendingUp,
  'laptop-code': Laptop,
  'file-text': FileText,
  'user': User,
  'palette': Palette,
  'megaphone': Megaphone,
  'heart': Heart,
  'camera': Camera,
  'dumbbell': Dumbbell,
  'music': Music,
  'graduation-cap': GraduationCap,
  'book': Book,
}

export function FeaturedCouponCard({ course }: FeaturedCouponCardProps) {
  const categoryColor = getCategoryColor(course.category)
  const categoryIconName = getCategoryIcon(course.category)
  const IconComponent = iconMap[categoryIconName] || Book

  return (
    <Link href={`/coupons/${course.courseId}`} className="block">
      <div className="bg-white dark:bg-card border border-gray-200 dark:border-border rounded-[10px] shadow-[0_4px_6px_rgba(0,0,0,0.1)] overflow-hidden transition-transform duration-300 hover:-translate-y-[5px] hover:shadow-lg relative cursor-pointer">
      <div className="relative h-[200px] w-full">
        <Image
          src={course.previewImage}
          alt={course.title}
          fill
          className="object-cover"
        />
        <div className="absolute top-[10px] right-[10px] bg-white/80 dark:bg-black/80 backdrop-blur-sm px-[10px] py-[5px] rounded-[20px] text-xs shadow-[0_2px_4px_rgba(0,0,0,0.1)]">
          Expires {formatRelativeTime(course.expiredDate)}
        </div>
      </div>
      
      <div className="p-2">
        <h3 className="text-base font-bold mb-2">
          <div style={{ display: 'grid', gridTemplateColumns: 'auto 1fr', gap: '0.5rem', alignItems: 'start' }}>
            <span style={{ color: categoryColor }} className="mt-0.5">
              <IconComponent className="w-5 h-5"/>
            </span>
            <div 
              className="line-clamp-2"
              style={{
                WebkitLineClamp: 2,
                WebkitBoxOrient: 'vertical',
                overflow: 'hidden',
                paddingLeft: '0.5rem',
                textIndent: '1.2rem',
                marginLeft: '-2rem',
              }}
            >
              {course.title}
            </div>
          </div>
        </h3>
        
        <p className="text-sm text-gray-600 dark:text-gray-400 mb-2.5 line-clamp-2">
          {course.heading}
        </p>
        
        <div className="flex flex-wrap gap-2.5 mb-2.5">
          <span
            className="inline-block px-2.5 py-1.5 rounded-[20px] text-xs text-white font-medium"
            style={{ backgroundColor: categoryColor }}
          >
            {course.category}
          </span>
        </div>
        
        <div className="flex justify-between items-center mb-2.5">
          <div className="flex items-center gap-2.5">
            <span className="text-sm flex items-center gap-1">
              <Star className="w-4 h-4 text-yellow-500 fill-yellow-500" />
              {course.rating.toFixed(1)}
            </span>
            <span className="text-sm flex items-center gap-1 text-gray-600 dark:text-gray-400">
              <Users className="w-4 h-4" />
              {course.students.toLocaleString()}
            </span>
          </div>
          <span className="text-sm flex items-center gap-1 text-gray-600 dark:text-gray-400">
            <Clock className="w-4 h-4" />
            {course.contentLength} mins
          </span>
        </div>
        
        <p className="text-xs text-center mt-1 text-red-600 dark:text-red-400">
          {course.usesRemaining} coupons left
        </p>
      </div>
    </div>
    </Link>
  )
}

