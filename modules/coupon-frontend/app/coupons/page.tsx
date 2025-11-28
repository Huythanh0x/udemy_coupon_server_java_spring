import { CouponGrid } from '@/components/coupons/CouponGrid'
import { FilterSidebar } from '@/components/coupons/FilterSidebar'
import { SearchBar } from '@/components/coupons/SearchBar'

export default function CouponsPage() {
  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-4xl font-bold mb-4">Browse Free Courses</h1>
        <p className="text-muted-foreground">
          Discover 100% off Udemy coupons for thousands of free online courses
        </p>
      </div>
      
      <div className="mb-6">
        <SearchBar />
      </div>

      <div className="flex flex-col lg:flex-row gap-8">
        <aside className="lg:w-64 flex-shrink-0">
          <FilterSidebar />
        </aside>
        
        <main className="flex-1">
          <CouponGrid />
        </main>
      </div>
    </div>
  )
}

