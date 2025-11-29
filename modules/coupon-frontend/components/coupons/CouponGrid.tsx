'use client'

import { useCoupons } from '@/hooks/useCoupons'
import { FeaturedCouponCard } from '@/components/coupons/FeaturedCouponCard'
import { Loader2, ChevronLeft, ChevronRight } from 'lucide-react'
import type { CouponListParams, SortField } from '@/types/coupon'

interface CouponGridProps {
  filters: CouponListParams
  onPageChange: (pageIndex: number) => void
  onFilterChange: (key: keyof CouponListParams, value: string | undefined) => void
}

const sortOptions: { value: SortField; label: string }[] = [
  { value: 'students', label: 'Most Students' },
  { value: 'rating', label: 'Highest Rating' },
  { value: 'createdAt', label: 'Newest' },
  { value: 'contentLength', label: 'Longest Content' },
  { value: 'usesRemaining', label: 'Most Coupons Left' },
]

export function CouponGrid({ filters, onPageChange, onFilterChange }: CouponGridProps) {
  const { data, isLoading, error } = useCoupons(filters)

  const currentPage = parseInt(filters.pageIndex || '0', 10)
  const totalPages = data?.totalPage || 0
  const totalCoupons = data?.totalCoupon || 0

  const handleSortChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const [sortBy, sortOrder] = e.target.value.split('-') as [SortField, 'asc' | 'desc']
    // Update both sort fields without resetting page
    onFilterChange('sortBy', sortBy, false)
    onFilterChange('sortOrder', sortOrder, false)
    onPageChange(0) // Reset to first page when sorting changes
  }

  // Build filter conditions array for display
  const getFilterConditions = () => {
    const conditions: { label: string; value: string; color: string }[] = []
    
    if (filters.query) {
      conditions.push({
        label: 'query',
        value: filters.query,
        color: '#3b82f6', // blue
      })
    }
    
    if (filters.category) {
      conditions.push({
        label: 'category',
        value: filters.category,
        color: '#10b981', // green
      })
    }
    
    if (filters.level) {
      conditions.push({
        label: 'level',
        value: filters.level,
        color: '#f59e0b', // amber
      })
    }
    
    if (filters.language) {
      conditions.push({
        label: 'language',
        value: filters.language,
        color: '#8b5cf6', // purple
      })
    }
    
    if (filters.rating) {
      conditions.push({
        label: 'rating',
        value: `${filters.rating}+ ⭐`,
        color: '#ef4444', // red
      })
    }
    
    return conditions
  }

  const renderResultsText = () => {
    if (isLoading) {
      return <span>Loading...</span>
    }
    
    if (!data) {
      return <span>No results</span>
    }

    const conditions = getFilterConditions()
    
    if (conditions.length === 0) {
      return (
        <span>
          <span className="font-bold text-lg underline" style={{ color: '#3b82f6' }}>
            {totalCoupons}
          </span>
          <span className="text-sm text-muted-foreground ml-1">courses</span>
        </span>
      )
    }

    // Format conditions with highlights
    const conditionElements = conditions.map((condition, index) => (
      <span key={index}>
        {index > 0 && <span className="text-muted-foreground"> and </span>}
        <span
          className="font-semibold px-2 py-0.5 rounded"
          style={{
            backgroundColor: `${condition.color}20`,
            color: condition.color,
          }}
        >
          {condition.value}
        </span>
      </span>
    ))

    return (
      <span>
        <span className="font-bold text-lg underline" style={{ color: '#3b82f6' }}>
          {totalCoupons}
        </span>
        <span className="text-sm text-muted-foreground ml-1">
          courses of {conditionElements}
        </span>
      </span>
    )
  }

  return (
    <div>
      {/* Header with sort and results count */}
      <div className="mb-6 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <p className="text-sm text-muted-foreground">
            {renderResultsText()}
          </p>
        </div>
        
        <div className="flex items-center gap-2">
          <label htmlFor="sort" className="text-sm font-medium whitespace-nowrap">
            Sort by:
          </label>
          <select
            id="sort"
            value={`${filters.sortBy || 'students'}-${filters.sortOrder || 'desc'}`}
            onChange={handleSortChange}
            className="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-card focus:outline-none focus:ring-2 focus:ring-primary text-sm"
          >
            {sortOptions.map((option) => (
              <optgroup key={option.value} label={option.label}>
                <option value={`${option.value}-desc`}>
                  {option.label} (High to Low)
                </option>
                <option value={`${option.value}-asc`}>
                  {option.label} (Low to High)
                </option>
              </optgroup>
            ))}
          </select>
        </div>
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="flex justify-center items-center py-12">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </div>
      )}

      {/* Error State */}
      {error && (
        <div className="text-center py-12 text-destructive">
          <p>Failed to load courses. Please try again later.</p>
        </div>
      )}

      {/* Empty State */}
      {!isLoading && !error && data && data.courses.length === 0 && (
        <div className="text-center py-12">
          <p className="text-muted-foreground text-lg mb-2">No courses found</p>
          <p className="text-sm text-muted-foreground">
            Try adjusting your filters or search query
          </p>
        </div>
      )}

      {/* Courses Grid */}
      {!isLoading && !error && data && data.courses.length > 0 && (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8">
            {data.courses.map((course) => (
              <FeaturedCouponCard key={course.courseId} course={course} />
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-2 mt-8">
              <button
                onClick={() => onPageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-card hover:bg-gray-50 dark:hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
              >
                <ChevronLeft className="w-4 h-4" />
                Previous
              </button>
              
              <div className="flex items-center gap-1">
                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                  let pageNum: number
                  if (totalPages <= 5) {
                    pageNum = i
                  } else if (currentPage < 3) {
                    pageNum = i
                  } else if (currentPage > totalPages - 4) {
                    pageNum = totalPages - 5 + i
                  } else {
                    pageNum = currentPage - 2 + i
                  }
                  
                  return (
                    <button
                      key={pageNum}
                      onClick={() => onPageChange(pageNum)}
                      className={`px-4 py-2 border rounded-lg ${
                        currentPage === pageNum
                          ? 'bg-primary text-primary-foreground border-primary'
                          : 'border-gray-300 dark:border-gray-600 bg-white dark:bg-card hover:bg-gray-50 dark:hover:bg-gray-800'
                      }`}
                    >
                      {pageNum + 1}
                    </button>
                  )
                })}
              </div>

              <button
                onClick={() => onPageChange(currentPage + 1)}
                disabled={currentPage >= totalPages - 1}
                className="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-card hover:bg-gray-50 dark:hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
              >
                Next
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

