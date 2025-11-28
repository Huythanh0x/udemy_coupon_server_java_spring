'use client'

export function CouponGrid() {
  // TODO: Fetch coupons from API using React Query
  return (
    <div>
      <div className="mb-4 flex justify-between items-center">
        <p className="text-muted-foreground">Loading courses...</p>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* TODO: Map over coupons and render CouponCard */}
        <p className="text-center text-muted-foreground col-span-full py-8">
          Coupons will be displayed here
        </p>
      </div>
    </div>
  )
}

