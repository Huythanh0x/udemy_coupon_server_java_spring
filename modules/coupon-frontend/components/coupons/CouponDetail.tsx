'use client'

import Image from 'next/image'
import Link from 'next/link'
import { useState } from 'react'
import { useCourseDetails, useCourseReviews, useCourseCurriculum, useRelatedCourses } from '@/hooks/useCoupons'
import { Loader2, Star, Users, Clock, ExternalLink, Calendar, Tag, Globe, Award, ChevronLeft, ChevronRight, Play, BookOpen, CheckCircle, Smartphone, Tv, GraduationCap } from 'lucide-react'
import { getCategoryColor, getCategoryIcon, getLevelEmoji } from '@/lib/course-utils'
import { formatRelativeTime } from '@/lib/utils'
import { Briefcase, Code, TrendingUp, Laptop, FileText, User, Palette, Megaphone, Heart, Camera, Dumbbell, Music, GraduationCap as GraduationCapIcon, Book } from 'lucide-react'

interface CouponDetailProps {
  courseId: string
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
  'graduation-cap': GraduationCapIcon,
  'book': Book,
}

export function CouponDetail({ courseId }: CouponDetailProps) {
  const [reviewsPage, setReviewsPage] = useState(1)
  const [selectedPreviewIndex, setSelectedPreviewIndex] = useState(0)
  
  const { data: courseDetails, isLoading: detailsLoading, error: detailsError } = useCourseDetails(
    courseId,
    undefined // couponCode - could be extracted from URL if needed
  )
  
  const { data: reviewsData, isLoading: reviewsLoading } = useCourseReviews(courseId, reviewsPage)
  const { data: curriculum, isLoading: curriculumLoading } = useCourseCurriculum(courseId)
  const { data: relatedCourses, isLoading: relatedLoading } = useRelatedCourses(courseId)

  if (detailsLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <Loader2 className="w-8 h-8 animate-spin text-primary" />
      </div>
    )
  }

  if (detailsError || !courseDetails) {
    return (
      <div className="text-center py-12">
        <p className="text-destructive text-lg mb-2">Failed to load course details</p>
        <p className="text-muted-foreground">Please try again later.</p>
        <Link href="/coupons" className="text-primary hover:underline mt-4 inline-block">
          ← Back to Courses
        </Link>
      </div>
    )
  }

  const course = courseDetails
  const categoryColor = getCategoryColor(course.category)
  const categoryIconName = getCategoryIcon(course.category)
  const IconComponent = iconMap[categoryIconName] || Book
  const levelEmoji = getLevelEmoji(course.level)

  const handleEnrollClick = () => {
    if (course.couponUrl) {
      window.open(course.couponUrl, '_blank', 'noopener,noreferrer')
    }
  }

  const handleReviewPageChange = (newPage: number) => {
    setReviewsPage(newPage)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return (
    <div className="max-w-6xl mx-auto">
      {/* Header Section */}
      <div className="mb-8">
        <Link 
          href="/coupons" 
          className="text-sm text-primary hover:underline mb-4 inline-block"
        >
          ← Back to Courses
        </Link>
        
        <div className="flex flex-col md:flex-row gap-6">
          {/* Course Image */}
          <div className="relative w-full md:w-1/3 h-[300px] md:h-[400px] rounded-lg overflow-hidden flex-shrink-0">
            <Image
              src={course.previewImage}
              alt={course.title}
              fill
              className="object-cover"
            />
            <div className="absolute top-4 right-4 bg-white/90 dark:bg-black/90 backdrop-blur-sm px-3 py-1.5 rounded-lg text-xs shadow-lg">
              <div className="flex items-center gap-1">
                <Calendar className="w-3 h-3" />
                Expires {formatRelativeTime(course.expiredDate)}
              </div>
            </div>
          </div>

          {/* Course Info */}
          <div className="flex-1">
            <div className="flex items-start gap-2 mb-3">
              <span style={{ color: categoryColor }}>
                <IconComponent className="w-6 h-6" />
              </span>
              <div className="flex flex-wrap gap-2 items-center">
                <span
                  className="text-sm font-bold"
                  style={{ color: categoryColor }}
                >
                  #{course.category}
                </span>
                {course.subCategory && course.subCategory.toLowerCase() !== 'unknown' && (
                  <span
                    className="text-sm font-bold"
                    style={{ color: categoryColor }}
                  >
                    #{course.subCategory}
                  </span>
                )}
              </div>
            </div>

            <h1 className="text-3xl md:text-4xl font-bold mb-3">{course.title}</h1>
            <p className="text-lg text-gray-600 dark:text-gray-400 mb-4">{course.heading}</p>

            {/* Author */}
            <div className="mb-4">
              <p className="text-base text-gray-700 dark:text-gray-300 italic underline">
                {course.author}
              </p>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
              <div className="flex items-center gap-2">
                <Star className="w-5 h-5 text-yellow-500 fill-yellow-500" />
                <div>
                  <p className="text-sm text-muted-foreground">Rating</p>
                  <p className="font-semibold">{course.rating.toFixed(1)}</p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <Users className="w-5 h-5 text-blue-500" />
                <div>
                  <p className="text-sm text-muted-foreground">Students</p>
                  <p className="font-semibold">{course.students.toLocaleString()}</p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <Clock className="w-5 h-5 text-green-500" />
                <div>
                  <p className="text-sm text-muted-foreground">Duration</p>
                  <p className="font-semibold">{course.contentLength} mins</p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <Award className="w-5 h-5 text-purple-500" />
                <div>
                  <p className="text-sm text-muted-foreground">Reviews</p>
                  <p className="font-semibold">{course.reviews.toLocaleString()}</p>
                </div>
              </div>
            </div>

            {/* Meta Info */}
            <div className="flex flex-wrap gap-4 mb-6 text-sm">
              <div className="flex items-center gap-2">
                <Tag className="w-4 h-4 text-muted-foreground" />
                <span className="font-medium">Level:</span>
                <span>{levelEmoji} {course.level}</span>
              </div>
              <div className="flex items-center gap-2">
                <Globe className="w-4 h-4 text-muted-foreground" />
                <span className="font-medium">Language:</span>
                <span>{course.language}</span>
              </div>
            </div>

            {/* Pricing Info */}
            {course.pricingInfo && (
              <div className="mb-4 p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
                {course.pricingInfo.couponCode && (
                  <p className="text-sm text-muted-foreground mb-1">Coupon Code:</p>
                )}
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-2xl font-bold text-blue-600 dark:text-blue-400">
                      {course.pricingInfo.priceString || 'Free'}
                    </p>
                    {course.pricingInfo.listPrice && course.pricingInfo.listPrice > 0 && (
                      <p className="text-sm text-muted-foreground line-through">
                        {course.pricingInfo.currencySymbol}{course.pricingInfo.listPrice.toLocaleString()}
                      </p>
                    )}
                  </div>
                  {course.pricingInfo.discountPercent && course.pricingInfo.discountPercent > 0 && (
                    <div className="text-right">
                      <p className="text-lg font-bold text-green-600 dark:text-green-400">
                        {course.pricingInfo.discountPercent}% OFF
                      </p>
                      {course.pricingInfo.discountDeadlineText && (
                        <p className="text-xs text-muted-foreground">
                          {course.pricingInfo.discountDeadlineText} left
                        </p>
                      )}
                    </div>
                  )}
                </div>
                {course.pricingInfo.couponCode && (
                  <p className="text-center text-xl font-bold text-blue-600 dark:text-blue-400 mt-2">
                    {course.pricingInfo.couponCode}
                  </p>
                )}
              </div>
            )}

            {/* Coupons Remaining */}
            <div className="mb-6 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
              <p className="text-center font-semibold text-red-600 dark:text-red-400">
                {course.usesRemaining} coupons left
              </p>
            </div>

            {/* Enroll Button */}
            <button
              onClick={handleEnrollClick}
              className="w-full md:w-auto px-8 py-4 bg-primary text-primary-foreground rounded-lg font-bold text-lg hover:bg-primary/90 transition-colors flex items-center justify-center gap-2 shadow-lg hover:shadow-xl"
            >
              <span>Enroll Now - 100% OFF</span>
              <ExternalLink className="w-5 h-5" />
            </button>

            {/* Incentives */}
            {course.incentives && (
              <div className="mt-6 p-4 bg-card border rounded-lg">
                <h3 className="font-semibold mb-3">What's Included</h3>
                <div className="grid grid-cols-2 gap-2 text-sm">
                  {course.incentives.videoContentLength && (
                    <div className="flex items-center gap-2">
                      <Play className="w-4 h-4 text-muted-foreground" />
                      <span>{course.incentives.videoContentLength} on-demand video</span>
                    </div>
                  )}
                  {course.incentives.hasLifetimeAccess && (
                    <div className="flex items-center gap-2">
                      <CheckCircle className="w-4 h-4 text-green-500" />
                      <span>Full lifetime access</span>
                    </div>
                  )}
                  {course.incentives.hasCertificate && (
                    <div className="flex items-center gap-2">
                      <GraduationCap className="w-4 h-4 text-blue-500" />
                      <span>Certificate of completion</span>
                    </div>
                  )}
                  {course.incentives.devicesAccess && (
                    <div className="flex items-center gap-2">
                      <Smartphone className="w-4 h-4 text-muted-foreground" />
                      <span>{course.incentives.devicesAccess}</span>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Preview Videos */}
      {course.previewVideos && course.previewVideos.length > 0 && (
        <div className="mb-8">
          <h2 className="text-2xl font-bold mb-4">Course Preview</h2>
          
          {/* Main Preview Video Player */}
          {course.previewVideos[selectedPreviewIndex] && (
            <div className="mb-6">
              <div className="relative w-full aspect-video rounded-lg overflow-hidden bg-gray-100 dark:bg-gray-800">
                <video
                  key={course.previewVideos[selectedPreviewIndex].id}
                  className="w-full h-full"
                  controls
                  poster={course.previewVideos[selectedPreviewIndex].thumbnailUrl || course.previewImage || undefined}
                >
                  {course.previewVideos[selectedPreviewIndex].videoUrl && (
                    <source src={course.previewVideos[selectedPreviewIndex].videoUrl} type="application/x-mpegURL" />
                  )}
                  {course.previewVideos[selectedPreviewIndex].streamUrls?.map((source, idx) => (
                    <source key={idx} src={source.file} type={source.type} />
                  ))}
                  Your browser does not support the video tag.
                </video>
              </div>
              {course.previewVideos[selectedPreviewIndex].title && (
                <h3 className="mt-2 text-lg font-semibold">{course.previewVideos[selectedPreviewIndex].title}</h3>
              )}
            </div>
          )}
          
          {/* Preview Videos List */}
          {course.previewVideos.length > 1 && (
            <div>
              <h3 className="text-lg font-semibold mb-3">Free Sample Videos:</h3>
              <div className="space-y-2">
                {course.previewVideos.map((preview, index) => (
                  <button
                    key={preview.id}
                    onClick={() => {
                      setSelectedPreviewIndex(index)
                      // Scroll to top to show the video player
                      window.scrollTo({ top: 0, behavior: 'smooth' })
                    }}
                    className={`w-full flex items-center gap-4 p-3 rounded-lg border transition-colors text-left ${
                      selectedPreviewIndex === index
                        ? 'bg-primary/10 border-primary dark:bg-primary/20'
                        : 'hover:bg-gray-50 dark:hover:bg-gray-800'
                    }`}
                  >
                    {preview.thumbnailUrl && (
                      <div className="relative w-32 h-20 flex-shrink-0 rounded overflow-hidden bg-gray-200 dark:bg-gray-700">
                        <Image
                          src={preview.thumbnailUrl}
                          alt={preview.title || 'Preview thumbnail'}
                          fill
                          className="object-cover"
                        />
                        <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-30">
                          <Play className="w-6 h-6 text-white" />
                        </div>
                      </div>
                    )}
                    <div className="flex-1 min-w-0">
                      <h4 className="font-medium text-sm line-clamp-2">{preview.title}</h4>
                      {preview.contentSummary && (
                        <p className="text-xs text-muted-foreground mt-1">{preview.contentSummary}</p>
                      )}
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
      
      {/* Fallback: Single Preview Video (if previewVideos not available) */}
      {(!course.previewVideos || course.previewVideos.length === 0) && course.previewVideo && (
        <div className="mb-8">
          <h2 className="text-2xl font-bold mb-4">Course Preview</h2>
          <div className="relative w-full aspect-video rounded-lg overflow-hidden bg-gray-100 dark:bg-gray-800">
            <video
              className="w-full h-full"
              controls
              poster={course.previewImage || undefined}
            >
              <source src={course.previewVideo} type="application/x-mpegURL" />
              Your browser does not support the video tag.
            </video>
          </div>
        </div>
      )}

      {/* Description */}
      {course.description && (
        <div className="mb-8">
          <h2 className="text-2xl font-bold mb-4">About This Course</h2>
          <div 
            className="text-gray-700 dark:text-gray-300 leading-relaxed [&>p]:mb-4 [&>ul]:list-disc [&>ul]:ml-6 [&>ul]:mb-4 [&>ol]:list-decimal [&>ol]:ml-6 [&>ol]:mb-4 [&>li]:mb-2 [&>h1]:text-2xl [&>h1]:font-bold [&>h1]:mb-4 [&>h2]:text-xl [&>h2]:font-bold [&>h2]:mb-3 [&>h3]:text-lg [&>h3]:font-bold [&>h3]:mb-2 [&>strong]:font-bold [&>em]:italic [&>a]:text-primary [&>a]:underline"
            dangerouslySetInnerHTML={{ __html: course.description }}
          />
        </div>
      )}

      {/* Curriculum */}
      {curriculum && curriculum.sections && curriculum.sections.length > 0 && (
        <div className="mb-8">
          <h2 className="text-2xl font-bold mb-4">Course Curriculum</h2>
          <div className="bg-card border rounded-lg p-6">
            <div className="mb-4 text-sm text-muted-foreground">
              <span>{curriculum.totalLectures} lectures</span>
              {curriculum.totalDuration && (
                <>
                  <span className="mx-2">•</span>
                  <span>{curriculum.totalDuration}</span>
                </>
              )}
            </div>
            <div className="space-y-4">
              {curriculum.sections.map((section, index) => (
                <div key={index} className="border-b last:border-b-0 pb-4 last:pb-0">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-semibold">{section.title}</h3>
                    <span className="text-sm text-muted-foreground">
                      {section.lectureCount} lectures • {section.duration}
                    </span>
                  </div>
                  <div className="space-y-2 ml-4">
                    {section.items.map((item, itemIndex) => (
                      <div key={itemIndex} className="flex items-center gap-2 text-sm">
                        {item.itemType === 'lecture' ? (
                          <Play className="w-4 h-4 text-muted-foreground" />
                        ) : (
                          <BookOpen className="w-4 h-4 text-muted-foreground" />
                        )}
                        <span className={item.canBePreviewed ? 'text-primary cursor-pointer hover:underline' : ''}>
                          {item.title}
                        </span>
                        {item.contentSummary && (
                          <span className="text-muted-foreground text-xs">
                            {item.contentSummary}
                          </span>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Reviews Section */}
      <div className="mb-8">
        <h2 className="text-2xl font-bold mb-4">
          Student Reviews
          {course.reviewsSummary && (
            <span className="text-lg font-normal text-muted-foreground ml-2">
              ({course.reviewsSummary.totalCount?.toLocaleString()} reviews)
            </span>
          )}
        </h2>

        {reviewsLoading ? (
          <div className="flex justify-center py-8">
            <Loader2 className="w-6 h-6 animate-spin text-primary" />
          </div>
        ) : reviewsData && reviewsData.reviews && reviewsData.reviews.length > 0 ? (
          <>
            <div className="space-y-6 mb-6">
              {reviewsData.reviews.map((review) => (
                <div key={review.id} className="bg-card border rounded-lg p-6">
                  <div className="flex items-start gap-4 mb-4">
                    {review.user?.image50x50 ? (
                      <Image
                        src={review.user.image50x50}
                        alt={review.user.displayName || 'User'}
                        width={50}
                        height={50}
                        className="rounded-full"
                      />
                    ) : (
                      <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center">
                        <span className="text-primary font-semibold">
                          {review.user?.initials || 'U'}
                        </span>
                      </div>
                    )}
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <p className="font-semibold">
                          {review.user?.publicDisplayName || review.user?.displayName || 'Anonymous'}
                        </p>
                        <div className="flex items-center gap-1">
                          {[...Array(5)].map((_, i) => (
                            <Star
                              key={i}
                              className={`w-4 h-4 ${
                                i < Math.floor(review.rating)
                                  ? 'text-yellow-500 fill-yellow-500'
                                  : 'text-gray-300'
                              }`}
                            />
                          ))}
                        </div>
                        <span className="text-sm text-muted-foreground">
                          {review.createdFormatted}
                        </span>
                      </div>
                      <div
                        className="text-gray-700 dark:text-gray-300"
                        dangerouslySetInnerHTML={{ __html: review.contentHtml || review.content }}
                      />
                      {review.response && (
                        <div className="mt-4 p-4 bg-muted rounded-lg">
                          <div className="flex items-center gap-2 mb-2">
                            <p className="font-semibold text-sm">
                              {review.response.user?.publicDisplayName || review.response.user?.displayName || 'Instructor'}
                            </p>
                            <span className="text-xs text-muted-foreground">
                              {review.response.createdFormatted}
                            </span>
                          </div>
                          <div
                            className="text-sm text-gray-700 dark:text-gray-300"
                            dangerouslySetInnerHTML={{ __html: review.response.contentHtml || review.response.content }}
                          />
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Pagination */}
            {reviewsData.totalCount && reviewsData.totalCount > reviewsData.reviews.length && (
              <div className="flex items-center justify-between">
                <button
                  onClick={() => handleReviewPageChange(reviewsPage - 1)}
                  disabled={!reviewsData.hasPrevious || reviewsPage === 1}
                  className="flex items-center gap-2 px-4 py-2 border rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
                >
                  <ChevronLeft className="w-4 h-4" />
                  Previous
                </button>
                <span className="text-sm text-muted-foreground">
                  Page {reviewsPage} of {Math.ceil((reviewsData.totalCount || 0) / 10)}
                </span>
                <button
                  onClick={() => handleReviewPageChange(reviewsPage + 1)}
                  disabled={!reviewsData.hasNext}
                  className="flex items-center gap-2 px-4 py-2 border rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-muted"
                >
                  Next
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            )}
          </>
        ) : (
          <p className="text-muted-foreground">No reviews available yet.</p>
        )}
      </div>

      {/* Related Courses */}
      {relatedCourses && relatedCourses.length > 0 && (
        <div className="mb-8">
          <h2 className="text-2xl font-bold mb-4">Related Courses</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {relatedCourses.map((relatedCourse) => (
              <Link key={relatedCourse.courseId} href={`/coupons/${relatedCourse.courseId}`}>
                <div className="bg-card border rounded-lg overflow-hidden hover:shadow-lg transition-shadow">
                  <div className="relative h-40 w-full">
                    <Image
                      src={relatedCourse.image240x135 || relatedCourse.image480x270 || ''}
                      alt={relatedCourse.title}
                      fill
                      className="object-cover"
                    />
                  </div>
                  <div className="p-4">
                    <h3 className="font-semibold mb-2 line-clamp-2">{relatedCourse.title}</h3>
                    <p className="text-sm text-muted-foreground mb-2">{relatedCourse.author}</p>
                    <div className="flex items-center gap-4 text-sm">
                      <div className="flex items-center gap-1">
                        <Star className="w-4 h-4 text-yellow-500 fill-yellow-500" />
                        <span>{relatedCourse.rating.toFixed(1)}</span>
                      </div>
                      <div className="flex items-center gap-1 text-muted-foreground">
                        <Users className="w-4 h-4" />
                        <span>{relatedCourse.numSubscribers.toLocaleString()}</span>
                      </div>
                      {relatedCourse.contentInfo && (
                        <span className="text-muted-foreground">{relatedCourse.contentInfo}</span>
                      )}
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
