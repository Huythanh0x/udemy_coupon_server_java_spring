'use client'

import { X } from 'lucide-react'
import type { CouponListParams } from '@/types/coupon'

interface FilterSidebarProps {
  filters: CouponListParams
  onFilterChange: (key: keyof CouponListParams, value: string | undefined) => void
  onClearAll?: () => void
}

const categories = [
  'Business',
  'Development',
  'Finance & Accounting',
  'IT & Software',
  'Office Productivity',
  'Personal Development',
  'Design',
  'Marketing',
  'Lifestyle',
  'Photography & Video',
  'Health & Fitness',
  'Music',
  'Teaching & Academics',
]

const levels = ['All Levels', 'Beginner', 'Intermediate', 'Expert']

const languages = ['English', 'Spanish', 'French', 'German', 'Italian', 'Portuguese', 'Chinese', 'Japanese', 'Korean', 'Hindi']

export function FilterSidebar({ filters, onFilterChange, onClearAll }: FilterSidebarProps) {
  const hasActiveFilters = filters.category || filters.level || filters.language || filters.rating

  const clearFilters = () => {
    if (onClearAll) {
      onClearAll()
    } else {
      // Fallback: clear each filter individually
      onFilterChange('category', undefined)
      onFilterChange('level', undefined)
      onFilterChange('language', undefined)
      onFilterChange('rating', undefined)
    }
  }

  return (
    <div className="bg-card border border-gray-200 dark:border-border rounded-lg p-4 sticky top-4">
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-semibold text-lg">Filters</h3>
        {hasActiveFilters && (
          <button
            onClick={clearFilters}
            className="text-sm text-primary hover:underline flex items-center gap-1"
          >
            <X className="w-4 h-4" />
            Clear
          </button>
        )}
      </div>
      
      <div className="space-y-4">
        <div>
          <label className="text-sm font-medium mb-2 block">Category</label>
          <select
            value={filters.category || ''}
            onChange={(e) => onFilterChange('category', e.target.value || undefined)}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-card focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">All Categories</option>
            {categories.map((cat) => (
              <option key={cat} value={cat}>
                {cat}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="text-sm font-medium mb-2 block">Level</label>
          <select
            value={filters.level || ''}
            onChange={(e) => onFilterChange('level', e.target.value || undefined)}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-card focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">All Levels</option>
            {levels.filter(l => l !== 'All Levels').map((level) => (
              <option key={level} value={level}>
                {level}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="text-sm font-medium mb-2 block">Language</label>
          <select
            value={filters.language || ''}
            onChange={(e) => onFilterChange('language', e.target.value || undefined)}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-card focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">All Languages</option>
            {languages.map((lang) => (
              <option key={lang} value={lang}>
                {lang}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="text-sm font-medium mb-2 block">Minimum Rating</label>
          <select
            value={filters.rating || ''}
            onChange={(e) => onFilterChange('rating', e.target.value || undefined)}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-card focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <option value="">Any Rating</option>
            <option value="4.5">4.5+ ⭐</option>
            <option value="4.3">4.3+ ⭐</option>
            <option value="4.0">4.0+ ⭐</option>
            <option value="3.5">3.5+ ⭐</option>
          </select>
        </div>
      </div>
    </div>
  )
}

