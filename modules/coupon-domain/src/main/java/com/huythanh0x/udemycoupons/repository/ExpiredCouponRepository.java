package com.huythanh0x.udemycoupons.repository;

import com.huythanh0x.udemycoupons.model.coupon.ExpiredCourseData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;

@Repository
public interface ExpiredCouponRepository extends JpaRepository<ExpiredCourseData, Integer> {

    /**
     * Efficiently retrieves all expired coupon URLs without loading full entities.
     * Used for pre-validation to avoid fetching URLs already marked as expired.
     *
     * @return Set of all expired coupon URLs
     */
    @Query("SELECT e.couponUrl FROM ExpiredCourseData e WHERE e.couponUrl IS NOT NULL")
    Set<String> findAllCouponUrls();

    /**
     * Retrieves coupon URLs that were marked as expired within the last N days.
     * These coupons may be reactivated, so they should be re-checked.
     * Uses createdAt field for consistency with other entities.
     *
     * @param sinceDate The timestamp threshold (current time - N days)
     * @return Set of coupon URLs expired in the last N days
     */
    @Query("SELECT e.couponUrl FROM ExpiredCourseData e WHERE e.createdAt >= :sinceDate AND e.couponUrl IS NOT NULL")
    Set<String> findCouponUrlsExpiredInLastDays(@Param("sinceDate") LocalDateTime sinceDate);
}
