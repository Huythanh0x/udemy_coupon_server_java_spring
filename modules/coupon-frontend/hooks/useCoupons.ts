import { useQuery } from '@tanstack/react-query'
import { couponApi } from '@/lib/api/client'
import type { CouponListParams } from '@/types/coupon'

export function useFeaturedCoupons() {
  return useQuery({
    queryKey: ['coupons', 'featured'],
    queryFn: async () => {
      // Fetch top 10 most popular courses (sorted by students descending)
      // Filter by rating >= 4.3
      // The API now handles sorting server-side, so we just need to request it
      const response = await couponApi.list({
        pageIndex: '0',
        numberPerPage: '10',
        sortBy: 'students',
        sortOrder: 'desc',
        rating: '4.3',
      })
      return response
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

export function useCoupons(params?: CouponListParams) {
  return useQuery({
    queryKey: ['coupons', 'list', params],
    queryFn: () => couponApi.list(params),
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useCouponDetail(courseId: string | number) {
  return useQuery({
    queryKey: ['coupons', 'detail', courseId],
    queryFn: () => couponApi.getById(courseId),
    enabled: !!courseId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

