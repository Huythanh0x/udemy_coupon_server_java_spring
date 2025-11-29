'use client'

import { useState, useEffect } from 'react'
import { useSearchParams, useRouter } from 'next/navigation'
import { CouponGrid } from '@/components/coupons/CouponGrid'
import { FilterSidebar } from '@/components/coupons/FilterSidebar'
import { SearchBar } from '@/components/coupons/SearchBar'
import type { CouponListParams } from '@/types/coupon'

export default function CouponsPage() {
  const searchParams = useSearchParams()
  const router = useRouter()
  
  const [filters, setFilters] = useState<CouponListParams>({
    query: searchParams.get('query') || undefined,
    category: searchParams.get('category') || undefined,
    level: searchParams.get('level') || undefined,
    language: searchParams.get('language') || undefined,
    rating: searchParams.get('rating') || undefined,
    sortBy: (searchParams.get('sortBy') as any) || 'students',
    sortOrder: (searchParams.get('sortOrder') as any) || 'desc',
    pageIndex: searchParams.get('pageIndex') || '0',
    numberPerPage: searchParams.get('numberPerPage') || '20',
  })

  // Update URL when filters change
  useEffect(() => {
    const params = new URLSearchParams()
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.set(key, value.toString())
      }
    })
    router.replace(`/coupons?${params.toString()}`, { scroll: false })
  }, [filters, router])

  const updateFilter = (key: keyof CouponListParams, value: string | undefined, resetPage = true) => {
    setFilters(prev => {
      // If updating search query, clear all filters
      if (key === 'query') {
        return {
          ...prev,
          query: value,
          category: undefined,
          level: undefined,
          language: undefined,
          rating: undefined,
          pageIndex: '0',
        }
      }
      
      // If updating any filter, clear search query
      if (key === 'category' || key === 'level' || key === 'language' || key === 'rating') {
        return {
          ...prev,
          [key]: value,
          query: undefined,
          ...(resetPage && { pageIndex: '0' }),
        }
      }
      
      // For other updates (sort, pagination), don't clear search or filters
      return {
        ...prev,
        [key]: value,
        ...(resetPage && { pageIndex: '0' }),
      }
    })
  }

  const clearSearch = () => {
    updateFilter('query', undefined)
  }

  const clearAllFilters = () => {
    setFilters(prev => ({
      ...prev,
      category: undefined,
      level: undefined,
      language: undefined,
      rating: undefined,
      query: undefined,
      pageIndex: '0',
    }))
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-4xl font-bold mb-4">Browse Free Courses</h1>
        <p className="text-muted-foreground">
          Discover 100% off Udemy coupons for thousands of premium online courses for free
        </p>
      </div>
      
      <div className="mb-6">
        <SearchBar 
          value={filters.query || ''} 
          onChange={(value) => updateFilter('query', value)}
          onClear={clearSearch}
        />
      </div>

      <div className="flex flex-col lg:flex-row gap-8">
        <aside className="lg:w-64 flex-shrink-0">
          <FilterSidebar 
            filters={filters}
            onFilterChange={updateFilter}
            onClearAll={clearAllFilters}
          />
        </aside>
        
        <main className="flex-1">
          <CouponGrid 
            filters={filters}
            onPageChange={(pageIndex) => updateFilter('pageIndex', pageIndex.toString(), false)}
            onFilterChange={updateFilter}
          />
        </main>
      </div>
    </div>
  )
}

