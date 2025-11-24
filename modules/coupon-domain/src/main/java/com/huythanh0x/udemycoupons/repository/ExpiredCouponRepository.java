package com.huythanh0x.udemycoupons.repository;

import com.huythanh0x.udemycoupons.model.coupon.ExpiredCourseData;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
     * Finds expired coupon URLs that were recently checked (updatedAt within the specified hours).
     * These URLs can be skipped from validation to avoid redundant API calls.
     *
     * @param updatedAfter The timestamp threshold (current time - X hours)
     * @return Set of expired coupon URLs that were recently checked
     */
    @Query("SELECT e.couponUrl FROM ExpiredCourseData e WHERE e.updatedAt >= :updatedAfter AND e.couponUrl IS NOT NULL")
    Set<String> findRecentlyCheckedExpiredUrls(@Param("updatedAfter") LocalDateTime updatedAfter);

    /**
     * Finds an expired coupon by URL.
     *
     * @param couponUrl The coupon URL to find
     * @return ExpiredCourseData if found, null otherwise
     */
    ExpiredCourseData findByCouponUrl(String couponUrl);

    /**
     * Updates the updatedAt timestamp for expired coupons with the given URLs.
     * Used to track when we last validated expired coupons.
     *
     * @param couponUrls Set of coupon URLs to update
     */
    @Modifying
    @Transactional
    @Query("UPDATE ExpiredCourseData e SET e.updatedAt = CURRENT_TIMESTAMP WHERE e.couponUrl IN :couponUrls")
    void updateUpdatedAtForUrls(@Param("couponUrls") Set<String> couponUrls);
}
