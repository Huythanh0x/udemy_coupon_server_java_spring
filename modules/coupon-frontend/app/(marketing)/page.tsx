import { Hero } from '@/components/marketing/Hero'
import { FeaturedCoupons } from '@/components/marketing/FeaturedCoupons'
import { StatsSection } from '@/components/marketing/StatsSection'

export default function HomePage() {
  return (
    <div className="flex flex-col">
      <Hero />
      <StatsSection />
      <FeaturedCoupons />
    </div>
  )
}

