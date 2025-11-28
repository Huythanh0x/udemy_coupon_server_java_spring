'use client'

export function FilterSidebar() {
  return (
    <div className="bg-card border rounded-lg p-4">
      <h3 className="font-semibold mb-4">Filters</h3>
      <div className="space-y-4">
        <div>
          <label className="text-sm font-medium mb-2 block">Category</label>
          <select className="w-full px-3 py-2 border rounded-lg">
            <option value="">All Categories</option>
            {/* TODO: Populate from API */}
          </select>
        </div>
        <div>
          <label className="text-sm font-medium mb-2 block">Level</label>
          <select className="w-full px-3 py-2 border rounded-lg">
            <option value="">All Levels</option>
            <option value="Beginner">Beginner</option>
            <option value="Intermediate">Intermediate</option>
            <option value="Expert">Expert</option>
          </select>
        </div>
        <div>
          <label className="text-sm font-medium mb-2 block">Language</label>
          <select className="w-full px-3 py-2 border rounded-lg">
            <option value="">All Languages</option>
            {/* TODO: Populate from API */}
          </select>
        </div>
      </div>
    </div>
  )
}

