package com.huythanh0x.udemycoupons.repository;

import com.huythanh0x.udemycoupons.model.coupon.CouponCourseData;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface CouponCourseRepository extends JpaRepository<CouponCourseData, Integer> {

    Page<CouponCourseData> findByTitleContainingOrDescriptionContainingOrHeadingContaining(String title, String description, String heading, Pageable pageable);

    Page<CouponCourseData> findByRatingGreaterThanAndContentLengthGreaterThanAndLevelContainingAndCategoryIsContainingIgnoreCaseAndLanguageContaining(float rating, int contentLength, String level, String category, String language, Pageable pageable);

    CouponCourseData findByCourseId(Integer courseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CouponCourseData ccd WHERE ccd.couponUrl IN :expiredCouponUrls")
    void deleteAllCouponsByUrl(@Param("expiredCouponUrls") Set<String> expiredCouponUrls);

    @Modifying
    @Transactional
    void deleteByCouponUrl(String couponUrl);

    /**
     * Efficiently retrieves all coupon URLs without loading full entities.
     * Used for pre-validation to avoid fetching URLs already in database.
     *
     * @return Set of all coupon URLs currently in the database
     */
    @Query("SELECT c.couponUrl FROM CouponCourseData c WHERE c.couponUrl IS NOT NULL")
    Set<String> findAllCouponUrls();

    /**
     * Finds coupon URLs that need to be refreshed based on expiration date or uses remaining.
     * Returns coupon URLs for coupons that are:
     * - Expiring within the specified hours threshold, OR
     * - Have uses remaining less than the specified minimum
     * 
     * Note: expiredDate is now stored as DATETIME, enabling simple and efficient date comparisons.
     *
     * @param expirationThresholdHours Hours from now to check expiration (e.g., 2 = expires within 2 hours)
     * @param minUsesRemaining Minimum uses remaining threshold (e.g., 50 = less than 50 uses)
     * @return Set of coupon URLs that need to be refreshed
     */
    @Query("SELECT c.couponUrl FROM CouponCourseData c WHERE " +
           "(c.expiredDate <= :expirationThreshold) OR " +
           "(c.usesRemaining < :minUsesRemaining)")
    Set<String> findCouponUrlsNeedingRefresh(
        @Param("expirationThreshold") java.time.Instant expirationThreshold,
        @Param("minUsesRemaining") int minUsesRemaining
    );
}
