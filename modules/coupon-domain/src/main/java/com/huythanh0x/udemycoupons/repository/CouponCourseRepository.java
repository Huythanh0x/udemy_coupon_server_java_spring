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
}
