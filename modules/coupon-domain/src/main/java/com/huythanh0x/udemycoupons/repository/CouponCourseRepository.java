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
     * Finds coupon URLs that need to be refreshed based on multiple criteria:
     * - Expiring within the specified threshold, OR
     * - Have uses remaining less than the specified minimum, OR
     * - Haven't been updated since the specified timestamp
     * 
     * This merged query is more efficient than calling separate methods.
     *
     * @param expirationThreshold Instant threshold for expiration (e.g., expires within 2 hours)
     * @param minUsesRemaining Minimum uses remaining threshold (e.g., 50 = less than 50 uses)
     * @param updatedBefore LocalDateTime threshold - coupons with updatedAt before this time
     * @return Set of coupon URLs that need to be refreshed for any of the above reasons
     */
    @Query("SELECT DISTINCT c.couponUrl FROM CouponCourseData c WHERE " +
           "c.couponUrl IS NOT NULL AND (" +
           "(c.expiredDate <= :expirationThreshold) OR " +
           "(c.usesRemaining < :minUsesRemaining) OR " +
           "(c.updatedAt < :updatedBefore)" +
           ")")
    Set<String> findCouponUrlsNeedingRefresh(
        @Param("expirationThreshold") java.time.Instant expirationThreshold,
        @Param("minUsesRemaining") int minUsesRemaining,
        @Param("updatedBefore") java.time.LocalDateTime updatedBefore
    );
}
