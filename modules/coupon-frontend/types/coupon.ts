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
  // Backend returns epoch seconds as a number (e.g. 1777014000.0).
  // It may also return ISO strings depending on endpoint/config.
  expiredDate: string | number
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

export type SortField = 'students' | 'rating' | 'createdAt' | 'contentLength' | 'usesRemaining'
export type SortOrder = 'asc' | 'desc'

export interface CouponListParams {
  category?: string
  rating?: string
  contentLength?: string
  level?: string
  language?: string
  query?: string
  sortBy?: SortField
  sortOrder?: SortOrder
  pageIndex?: string
  numberPerPage?: string
}

// Course Detail DTOs
export interface CourseDetailDTO extends CouponCourseData {
  reviewsSummary?: ReviewsSummaryDTO
  curriculum?: CurriculumDTO
  relatedCourses?: RelatedCourseDTO[]
  pricingInfo?: PricingInfoDTO
  incentives?: IncentivesDTO
  previewVideos?: PreviewVideoDTO[]
}

export interface PreviewVideoDTO {
  id: number
  title: string
  thumbnailUrl?: string
  contentSummary?: string // e.g., "02:22"
  timeEstimation?: number // in seconds
  videoUrl?: string // HLS m3u8 URL
  streamUrls?: VideoSourceDTO[] // MP4 files at different resolutions
}

export interface VideoSourceDTO {
  type: string // e.g., "video/mp4"
  label: string // e.g., "720", "480", "360", "144", "Auto"
  file: string // URL to the video file
}

export interface ReviewsSummaryDTO {
  totalCount?: number
  averageRating?: number
  recentReviews?: ReviewDTO[]
}

export interface ReviewDTO {
  id: number
  content: string
  contentHtml: string
  rating: number
  created: string
  createdFormatted: string
  user?: ReviewUserDTO
  response?: ReviewResponseDTO
}

export interface ReviewUserDTO {
  displayName: string
  publicDisplayName: string
  image50x50: string
  initials: string
}

export interface ReviewResponseDTO {
  content: string
  contentHtml: string
  created: string
  createdFormatted: string
  user?: ReviewUserDTO
}

export interface CurriculumDTO {
  sections: CurriculumSectionDTO[]
  totalDuration?: string
  totalDurationSeconds?: number
  totalLectures?: number
}

export interface CurriculumSectionDTO {
  title: string
  index: number
  duration: string
  durationSeconds: number
  lectureCount: number
  items: CurriculumItemDTO[]
}

export interface CurriculumItemDTO {
  id: number
  title: string
  description: string
  contentSummary: string
  itemType: string
  canBePreviewed: boolean
  isCodingExercise: boolean
  isPracticeTest: boolean
  previewUrl: string
  learnUrl: string
  objectIndex: number
}

export interface RelatedCourseDTO {
  courseId: number
  title: string
  headline: string
  url: string
  image240x135: string
  image480x270: string
  image750x422: string
  author: string
  rating: number
  numReviews: number
  numSubscribers: number
  contentInfo: string
  instructionalLevel: string
}

export interface PricingInfoDTO {
  price?: number
  listPrice?: number
  savingPrice?: number
  currency?: string
  priceString?: string
  currencySymbol?: string
  discountPercent?: number
  discountDeadlineText?: string
  couponCode?: string
  usesRemaining?: number
  maximumUses?: number
}

export interface IncentivesDTO {
  videoContentLength?: string
  numArticles?: number
  numQuizzes?: number
  numPracticeTests?: number
  numCodingExercises?: number
  hasLifetimeAccess?: boolean
  devicesAccess?: string
  hasAssignments?: boolean
  hasCertificate?: boolean
  hasClosedCaptions?: boolean
}

export interface CourseReviewsDTO {
  reviews: ReviewDTO[]
  totalCount: number
  currentPage: number
  hasNext: boolean
  hasPrevious: boolean
  nextUrl?: string
  previousUrl?: string
}

