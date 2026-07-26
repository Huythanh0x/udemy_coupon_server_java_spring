package com.huythanh0x.udemycoupons.controller;

import com.huythanh0x.udemycoupons.dto.CouponDetailDTO;
import com.huythanh0x.udemycoupons.dto.CouponRequestDTO;
import com.huythanh0x.udemycoupons.dto.CouponUpdateRequestDTO;
import com.huythanh0x.udemycoupons.dto.PagedCouponResponseDTO;
import com.huythanh0x.udemycoupons.service.CourseResponseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
     * Returns 202 Accepted as validation is performed asynchronously.
     */
    @PostMapping
    public ResponseEntity<String> createCoupon(@RequestBody CouponRequestDTO requestBody, HttpServletRequest request) {
        courseResponseService.saveNewCouponUrlAsync(requestBody.getCouponUrl(), request.getRemoteAddr());
        return ResponseEntity.accepted().body("Coupon submission received and is being processed.");
    }

    /**
     * Deletes a coupon by its course identifier.
     */
    @DeleteMapping("/{courseId}")
    public void deleteCoupon(@PathVariable("courseId") Integer courseId) {
        courseResponseService.deleteCouponByCourseId(courseId);
    }

    /**
     * Updates an existing coupon.
     */
    @PutMapping("/{courseId}")
    public CouponDetailDTO updateCoupon(
        @PathVariable("courseId") Integer courseId,
        @RequestBody CouponUpdateRequestDTO requestBody
    ) {
        return courseResponseService.updateCoupon(courseId, requestBody);
    }

    /**
     * Retrieves coupon details for a specific course identified by courseId.
     */
    @GetMapping("/{courseId}")
    public CouponDetailDTO getCouponDetail(@PathVariable("courseId") String courseId) {
        return courseResponseService.getCouponDetail(courseId);
    }
}

