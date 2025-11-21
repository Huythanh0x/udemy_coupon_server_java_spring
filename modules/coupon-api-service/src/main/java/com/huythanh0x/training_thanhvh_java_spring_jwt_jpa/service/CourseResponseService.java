package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.service;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.UdemyCouponCourseExtractor;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.dto.PagedCouponResponseDTO;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.exception.BadRequestException;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.coupon.CouponCourseData;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository.CouponCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service class for handling course response operations.
 */
@Service
public class CourseResponseService {
    private final CouponCourseRepository couponCourseRepository;


    @Autowired
    public CourseResponseService(CouponCourseRepository couponCourseRepository) {
        this.couponCourseRepository = couponCourseRepository;
    }

    /**
     * Retrieves a paged list of coupons based on the specified page index and number of items per page.
     * @param pageIndex The index of the page to retrieve.
     * @param numberPerPage The number of items to display per page.
     * @param remoteAddr The remote address of the client making the request.
     * @return A PagedCouponResponseDTO object containing the requested coupons.
     */
    public PagedCouponResponseDTO getPagedCoupons(String pageIndex, String numberPerPage, String remoteAddr) {
        handlePagingParameters(pageIndex, numberPerPage);
        Pageable pageable = PageRequest.of(Integer.parseInt(pageIndex), Integer.parseInt(numberPerPage));
        Page<CouponCourseData> allCouponCourses = couponCourseRepository.findAll(pageable);
        return new PagedCouponResponseDTO(allCouponCourses);
    }

    /**
     * Filters coupons based on the provided parameters such as rating, content length, level, category, language, page index, number per page,
     * and remote address.
     *
     * @param rating the minimum rating value for the coupons
     * @param contentLength the minimum content length for the coupons
     * @param level the level of the coupons
     * @param category the category of the coupons
     * @param language the language of the coupons
     * @param pageIndex the index of the page
     * @param numberPerPage the number of coupons to display per page
     * @param remoteAddr the remote address of the user
     * @return a PagedCouponResponseDTO containing filtered coupon courses based on the provided parameters
     */
    public PagedCouponResponseDTO filterCoupons(String rating, String contentLength, String level, String category, String language, String pageIndex, String numberPerPage, String remoteAddr) {
        handlePagingParameters(pageIndex, numberPerPage);
        Pageable pageable = PageRequest.of(Integer.parseInt(pageIndex), Integer.parseInt(numberPerPage));
        Page<CouponCourseData> filterCouponCourses = couponCourseRepository.findByRatingGreaterThanAndContentLengthGreaterThanAndLevelContainingAndCategoryIsContainingIgnoreCaseAndLanguageContaining(Float.parseFloat(rating), Integer.parseInt(contentLength), level, category, language, pageable);
        return new PagedCouponResponseDTO(filterCouponCourses);
    }

    /**
     * Searches for coupons based on a query string and returns a paged response.
     *
     * @param querySearch The query string to search for in coupon titles, descriptions, and headings.
     * @param pageIndex The index of the page to fetch.
     * @param numberPerPage The number of items to display per page.
     * @param remoteAddr The remote address of the user making the request.
     * @return PagedCouponResponseDTO The paged response containing the searched coupon courses.
     */
    public PagedCouponResponseDTO searchCoupons(String querySearch, String pageIndex, String numberPerPage, String remoteAddr) {
        handlePagingParameters(pageIndex, numberPerPage);
        Pageable pageable = PageRequest.of(Integer.parseInt(pageIndex), Integer.parseInt(numberPerPage));
        Page<CouponCourseData> searchedCouponCourses = couponCourseRepository.findByTitleContainingOrDescriptionContainingOrHeadingContaining(querySearch, querySearch, querySearch, pageable);
        return new PagedCouponResponseDTO(searchedCouponCourses);
    }

    /**
     * Saves a new coupon URL to the database with the provided remote address.
     * @param couponUrl the URL of the coupon to save
     * @param remoteAddr the remote address of the user saving the coupon
     * @return the saved CouponCourseData object containing the full coupon code data
     */
    public CouponCourseData saveNewCouponUrl(String couponUrl, String remoteAddr) {
        return couponCourseRepository.save(new UdemyCouponCourseExtractor(couponUrl).getFullCouponCodeData());
    }

    /**
     * Deletes a coupon based on its URL if it cannot be extracted.
     *
     * @param couponUrl The URL of the coupon to be deleted.
     * @param remoteAddr The IP address of the client making the request.
     */
    public void deleteCoupon(String couponUrl, String remoteAddr) {
        if (new UdemyCouponCourseExtractor(couponUrl).getFullCouponCodeData() == null) {
            couponCourseRepository.deleteByCouponUrl(couponUrl);
        }
    }

    /**
     * Validates and handles paging parameters for pagination.
     *
     * @param pageIndex the page index to be validated
     * @param numberPerPage the number of items per page to be validated
     * @throws BadRequestException if the pageIndex or numberPerPage is not a valid integer, or if they are negative
     */
    public void handlePagingParameters(String pageIndex, String numberPerPage) {
        try {
            Integer.parseInt(pageIndex);
            Integer.parseInt(numberPerPage);
        } catch (Exception e) {
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
