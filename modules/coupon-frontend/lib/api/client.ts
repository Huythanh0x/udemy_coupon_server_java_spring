import axios from 'axios'
import { API_ENDPOINTS } from '../constants'
import type { PagedCouponResponse, CouponCourseData, CouponListParams } from '@/types/coupon'

const apiClient = axios.create({
  baseURL: API_ENDPOINTS.coupons.list.replace('/api/v1/coupons', ''),
  headers: {
    'Content-Type': 'application/json',
  },
})

export const couponApi = {
  /**
   * List coupons with optional filters and pagination
   */
  list: async (params?: CouponListParams): Promise<PagedCouponResponse> => {
    const queryParams = new URLSearchParams()
    
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          queryParams.append(key, value.toString())
        }
      })
    }

    const response = await apiClient.get<PagedCouponResponse>(
      `/api/v1/coupons?${queryParams.toString()}`
    )
    return response.data
  },

  /**
   * Get coupon details by course ID
   */
  getById: async (courseId: string | number): Promise<CouponCourseData> => {
    const response = await apiClient.get<CouponCourseData>(
      API_ENDPOINTS.coupons.detail(courseId)
    )
    return response.data
  },
}

