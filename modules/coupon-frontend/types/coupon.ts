export interface CouponCourseData {
  courseId: number
  category: string
  subCategory?: string
  title: string
  contentLength: number
  level: string
  author: string
  rating: number
  reviews: number
  students: number
  couponCode: string
  previewImage: string
  couponUrl: string
  expiredDate: string
  usesRemaining: number
  heading: string
  description: string
  previewVideo?: string
  language: string
  isNew: boolean
  createdAt?: string
  updatedAt?: string
}

export interface PagedCouponResponse {
  lastFetchTime: number
  totalCoupon: number
  totalPage: number
  currentPage: number
  courses: CouponCourseData[]
}

export interface CouponListParams {
  category?: string
  rating?: string
  contentLength?: string
  level?: string
  language?: string
  query?: string
  pageIndex?: string
  numberPerPage?: string
}

