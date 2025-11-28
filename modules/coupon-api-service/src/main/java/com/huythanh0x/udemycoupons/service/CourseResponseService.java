package com.huythanh0x.udemycoupons.service;

import com.huythanh0x.udemycoupons.crawler_runner.UdemyCouponCourseExtractor;
import com.huythanh0x.udemycoupons.dto.CouponUpdateRequestDTO;
import com.huythanh0x.udemycoupons.dto.PagedCouponResponseDTO;
import com.huythanh0x.udemycoupons.exception.BadRequestException;
import com.huythanh0x.udemycoupons.model.coupon.CouponCourseData;
import com.huythanh0x.udemycoupons.model.coupon.CouponCourseHistory;
import com.huythanh0x.udemycoupons.repository.CouponCourseHistoryRepository;
import com.huythanh0x.udemycoupons.repository.CouponCourseRepository;
import com.huythanh0x.udemycoupons.repository.ExpiredCouponRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.huythanh0x.udemycoupons.utils.Constant;

/**
 * Service class for handling course response operations.
 */
@Service
public class CourseResponseService {
    private static final Logger log = LoggerFactory.getLogger(CourseResponseService.class);
    private final CouponCourseRepository couponCourseRepository;
    private final ExpiredCouponRepository expiredCouponRepository;
    private final CouponCourseHistoryRepository couponCourseHistoryRepository;


    @Autowired
    public CourseResponseService(CouponCourseRepository couponCourseRepository,
                                 ExpiredCouponRepository expiredCouponRepository,
                                 CouponCourseHistoryRepository couponCourseHistoryRepository) {
        this.couponCourseRepository = couponCourseRepository;
        this.expiredCouponRepository = expiredCouponRepository;
        this.couponCourseHistoryRepository = couponCourseHistoryRepository;
    }

    /**
     * Unified listing endpoint that supports basic pagination, structured filters and free-text search.
     */
    public PagedCouponResponseDTO listCoupons(
        String category,
        String rating,
        String contentLength,
        String level,
        String language,
        String query,
        String pageIndex,
        String numberPerPage,
        String remoteAddr
    ) {
        handlePagingParameters(pageIndex, numberPerPage);
        Pageable pageable = PageRequest.of(Integer.parseInt(pageIndex), Math.min(Integer.parseInt(numberPerPage), Constant.MAX_PAGE_SIZE));

        boolean hasQuery = query != null && !query.isBlank();
        boolean hasStructuredFilters =
            (rating != null && !rating.equals("-1")) ||
                (contentLength != null && !contentLength.equals("-1")) ||
                (level != null && !level.isBlank()) ||
                (category != null && !category.isBlank()) ||
                (language != null && !language.isBlank());

        Page<CouponCourseData> page;

        if (hasQuery && hasStructuredFilters) {
            // For now, prefer full-text style search when a query is present.
            page = couponCourseRepository.findByTitleContainingOrDescriptionContainingOrHeadingContaining(
                query, query, query, pageable
            );
        } else if (hasQuery) {
            page = couponCourseRepository.findByTitleContainingOrDescriptionContainingOrHeadingContaining(
                query, query, query, pageable
            );
        } else if (hasStructuredFilters) {
            page = couponCourseRepository
                .findByRatingGreaterThanAndContentLengthGreaterThanAndLevelContainingAndCategoryIsContainingIgnoreCaseAndLanguageContaining(
                    Float.parseFloat(rating),
                    Integer.parseInt(contentLength),
                    level,
                    category,
                    language,
                    pageable
                );
        } else {
            page = couponCourseRepository.findAll(pageable);
        }

        return new PagedCouponResponseDTO(page);
    }

    /**
     * Saves a new coupon URL to the database with the provided remote address.
     *
     * @param couponUrl  the URL of the coupon to save
     * @param remoteAddr the remote address of the user saving the coupon
     * @return the saved CouponCourseData object containing the full coupon code data
     */
    public CouponCourseData saveNewCouponUrl(String couponUrl, String remoteAddr) {
        UdemyCouponCourseExtractor extractor = new UdemyCouponCourseExtractor(couponUrl);
        CouponCourseData couponData = extractor.getFullCouponCodeData();
        if (couponData == null) {
            throw new BadRequestException("Coupon is invalid or expired");
        }

        boolean existedBefore = couponCourseRepository.findByCouponUrl(couponUrl) != null
                || expiredCouponRepository.findByCouponUrl(couponUrl) != null;
        couponData.setNew(!existedBefore);

        CouponCourseData saved = couponCourseRepository.save(couponData);
        couponCourseHistoryRepository.save(CouponCourseHistory.builder()
            .courseId(saved.getCourseId())
            .title(saved.getTitle())
            .couponUrl(saved.getCouponUrl())
            .status(existedBefore ? "reactivated" : "new")
            .build());
        return saved;
    }

    /**
     * Deletes a coupon by its course identifier.
     *
     * @param courseId the ID of the course to delete
     */
    public void deleteCouponByCourseId(Integer courseId) {
        couponCourseRepository.deleteById(courseId);
    }

    /**
     * Updates a coupon.
     * <p>
     * Currently this performs a simple existence check and returns the existing entity,
     * but it provides a clear extension point to modify fields in the future.
     *
     * @param courseId the ID of the coupon to update
     * @param request  the update request payload
     * @return updated coupon data
     */
    public CouponCourseData updateCoupon(Integer courseId, CouponUpdateRequestDTO request) {
        CouponCourseData existing = couponCourseRepository.findByCourseId(courseId);
        if (existing == null) {
            throw new BadRequestException("Course id not found");
        }
        // In the future, copy allowed fields from request into existing before saving.
        return couponCourseRepository.save(existing);
    }

    /**
     * Validates and handles paging parameters for pagination.
     *
     * @param pageIndex     the page index to be validated
     * @param numberPerPage the number of items per page to be validated
     * @throws BadRequestException if the pageIndex or numberPerPage is not a valid integer, or if they are negative
     */
    public void handlePagingParameters(String pageIndex, String numberPerPage) {
        try {
            Integer.parseInt(pageIndex);
            Integer.parseInt(numberPerPage);
        } catch (Exception e) {
            log.warn("Invalid paging parameters pageIndex={}, numberPerPage={}", pageIndex, numberPerPage, e);
            throw new BadRequestException(e.toString());
        }
        if (Integer.parseInt(pageIndex) < 0 || Integer.parseInt(numberPerPage) < 0) {
            throw new BadRequestException("Page index and number of course per page cannot be negative");
        }
    }

    /**
     * Retrieves the coupon course data by course ID.
     *
     * @param courseId The ID of the course to retrieve coupon data for.
     * @return The coupon course data for the specified course ID.
     * @throws BadRequestException if the course ID is not found.
     */
    public CouponCourseData getCouponDetail(String courseId) {
        CouponCourseData couponCourseData = couponCourseRepository.findByCourseId(Integer.parseInt(courseId));
        if (couponCourseData != null) {
            return couponCourseData;
        } else {
            throw new BadRequestException("Course id not found");
        }
    }
}

