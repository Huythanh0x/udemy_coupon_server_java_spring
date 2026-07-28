package com.thanh0x.coursedeal.repository

import com.thanh0x.coursedeal.model.coupon.CouponCourseHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CouponCourseHistoryRepository : JpaRepository<CouponCourseHistory, Long>
