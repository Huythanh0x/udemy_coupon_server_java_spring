package com.huythanh0x.udemycoupons.controller;

import com.huythanh0x.udemycoupons.dto.CouponRequestDTO;
import com.huythanh0x.udemycoupons.dto.CouponUpdateRequestDTO;
import com.huythanh0x.udemycoupons.dto.PagedCouponResponseDTO;
import com.huythanh0x.udemycoupons.model.coupon.CouponCourseData;
import com.huythanh0x.udemycoupons.service.CourseResponseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for managing coupon-related endpoints.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "api/v1/coupons")
public class CouponCourseController {
    private final CourseResponseService courseResponseService;

    @Autowired
    public CouponCourseController(CourseResponseService courseResponseService) {
        this.courseResponseService = courseResponseService;
    }

    /**
     * Retrieves a paged list of coupons with optional filtering, search, and sorting criteria.
     * <p>
     * This endpoint merges the previous "list", "filter" and "search" endpoints into a single,
     * flexible query API with sorting support.
     *
     * @param category      optional category filter
     * @param rating        optional minimum rating filter
     * @param contentLength optional minimum content length filter
     * @param level         optional level filter
     * @param language      optional language filter
     * @param query         optional free-text search query
     * @param sortBy        optional sort field: students, rating, createdAt, contentLength, usesRemaining (default: createdAt)
     * @param sortOrder     optional sort order: asc, desc (default: desc)
     * @param pageIndex     page index (0-based), defaults to 0
     * @param numberPerPage number of items per page, defaults to 10
     * @param request       HTTP servlet request
     * @return a paginated coupon response
     */
    @GetMapping
    public PagedCouponResponseDTO listCoupons(
        @RequestParam(required = false, defaultValue = "") String category,
        @RequestParam(required = false, defaultValue = "-1") String rating,
        @RequestParam(required = false, defaultValue = "-1") String contentLength,
        @RequestParam(required = false, defaultValue = "") String level,
        @RequestParam(required = false, defaultValue = "") String language,
        @RequestParam(required = false, defaultValue = "") String query,
        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
        @RequestParam(required = false, defaultValue = "desc") String sortOrder,
        @RequestParam(required = false, defaultValue = "0") String pageIndex,
        @RequestParam(required = false, defaultValue = "10") String numberPerPage,
        HttpServletRequest request
    ) {
        return courseResponseService.listCoupons(
            category,
            rating,
            contentLength,
            level,
            language,
            query,
            sortBy,
            sortOrder,
            pageIndex,
            numberPerPage,
            request.getRemoteAddr()
        );
    }

    /**
     * Creates a new coupon based on the provided Udemy coupon URL.
     *
     * @param requestBody request body containing the coupon URL
     * @param request     the HTTP servlet request
     * @return the saved course data with the new coupon
     */
    @PostMapping
    public CouponCourseData createCoupon(@RequestBody CouponRequestDTO requestBody, HttpServletRequest request) {
        return courseResponseService.saveNewCouponUrl(requestBody.getCouponUrl(), request.getRemoteAddr());
    }

    /**
     * Deletes a coupon by its course identifier.
     *
     * @param courseId the course identifier of the coupon to be deleted
     */
    @DeleteMapping("/{courseId}")
    public void deleteCoupon(@PathVariable("courseId") Integer courseId) {
        courseResponseService.deleteCouponByCourseId(courseId);
    }

    /**
     * Updates an existing coupon.
     * <p>
     * Currently this endpoint is intentionally minimal and can be extended with more
     * updatable fields as needed.
     *
     * @param courseId     the identifier of the coupon to update
     * @param requestBody  details of the update
     * @return the updated coupon data
     */
    @PutMapping("/{courseId}")
    public CouponCourseData updateCoupon(
        @PathVariable("courseId") Integer courseId,
        @RequestBody CouponUpdateRequestDTO requestBody
    ) {
        return courseResponseService.updateCoupon(courseId, requestBody);
    }

    /**
     * Retrieves coupon details for a specific course identified by courseId.
     *
     * @param courseId The unique identifier of the course.
     * @return The CouponCourseData object containing details of the course's coupon.
     */
    @GetMapping("/{courseId}")
    public CouponCourseData getCouponDetail(@PathVariable("courseId") String courseId) {
        return courseResponseService.getCouponDetail(courseId);
    }
}

