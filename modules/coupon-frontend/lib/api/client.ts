import axios from 'axios'
import { API_BASE_URL } from '../constants'
import type { 
  PagedCouponResponse, 
  CouponCourseData, 
  CouponListParams,
  CourseDetailDTO,
  CourseReviewsDTO,
  CurriculumDTO,
  RelatedCourseDTO
} from '@/types/coupon'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
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
      `/api/v1/coupons/${courseId}`
    )
    return response.data
  },

  /**
   * Get comprehensive course details including reviews, curriculum, and related courses
   */
  getDetails: async (courseId: string | number, couponCode?: string): Promise<CourseDetailDTO> => {
    const queryParams = new URLSearchParams()
    if (couponCode) {
      queryParams.append('couponCode', couponCode)
    }
    const url = `/api/v1/coupons/${courseId}/details${queryParams.toString() ? '?' + queryParams.toString() : ''}`
    const response = await apiClient.get<CourseDetailDTO>(url)
    return response.data
  },

  /**
   * Get paginated course reviews
   */
  getReviews: async (courseId: string | number, page: number = 1): Promise<CourseReviewsDTO> => {
    const response = await apiClient.get<CourseReviewsDTO>(
      `/api/v1/coupons/${courseId}/reviews?page=${page}`
    )
    return response.data
  },

  /**
   * Get course curriculum/syllabus
   */
  getCurriculum: async (courseId: string | number, couponCode?: string): Promise<CurriculumDTO> => {
    const queryParams = new URLSearchParams()
    if (couponCode) {
      queryParams.append('couponCode', couponCode)
    }
    const url = `/api/v1/coupons/${courseId}/curriculum${queryParams.toString() ? '?' + queryParams.toString() : ''}`
    const response = await apiClient.get<CurriculumDTO>(url)
    return response.data
  },

  /**
   * Get related/recommended courses
   */
  getRelated: async (courseId: string | number): Promise<RelatedCourseDTO[]> => {
    const response = await apiClient.get<RelatedCourseDTO[]>(
      `/api/v1/coupons/${courseId}/related`
    )
    return response.data
  },
}

