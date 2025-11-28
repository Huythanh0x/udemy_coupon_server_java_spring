export function StatsSection() {
  // TODO: Fetch real stats from API
  const stats = [
    { label: 'Free Courses', value: '10,000+' },
    { label: 'Categories', value: '50+' },
    { label: 'Active Coupons', value: '500+' },
  ]

  return (
    <section className="py-16 bg-muted/50">
      <div className="container mx-auto px-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-4xl mx-auto">
          {stats.map((stat, index) => (
            <div key={index} className="text-center">
              <div className="text-4xl font-bold text-primary mb-2">
                {stat.value}
              </div>
              <div className="text-muted-foreground">{stat.label}</div>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

