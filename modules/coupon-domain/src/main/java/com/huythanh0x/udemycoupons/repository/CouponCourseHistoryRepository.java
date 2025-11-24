package com.huythanh0x.udemycoupons.repository;

import com.huythanh0x.udemycoupons.model.coupon.CouponCourseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponCourseHistoryRepository extends JpaRepository<CouponCourseHistory, Long> {
}

