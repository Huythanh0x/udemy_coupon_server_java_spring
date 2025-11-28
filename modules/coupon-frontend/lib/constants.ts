export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export const API_ENDPOINTS = {
  coupons: {
    list: `${API_BASE_URL}/api/v1/coupons`,
    detail: (courseId: string | number) =>
      `${API_BASE_URL}/api/v1/coupons/${courseId}`,
  },
} as const

