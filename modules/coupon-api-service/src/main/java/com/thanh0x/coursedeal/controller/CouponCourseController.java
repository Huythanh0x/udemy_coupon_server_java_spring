package com.thanh0x.coursedeal.controller;

import com.thanh0x.coursedeal.dto.CouponDetailDTO;
import com.thanh0x.coursedeal.dto.CouponRequestDTO;
import com.thanh0x.coursedeal.dto.CouponUpdateRequestDTO;
import com.thanh0x.coursedeal.dto.PagedCouponResponseDTO;
import com.thanh0x.coursedeal.service.CourseResponseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
     * <p>
     * Direct deletion is currently disabled.
     *
     * @param courseId the course identifier of the coupon to be deleted
     */
    @DeleteMapping("/{courseId}")
    public void deleteCoupon(@PathVariable("courseId") Integer courseId) {
        throw new UnsupportedOperationException("Direct deletion is not allowed.");
    }

    @Value("${custom.refresh-secret:}")
    private String refreshSecret;

    /**
     * Triggers a refresh of the coupon data from Udemy.
     * Requires a valid hash/secret to prevent unauthorized scraping load.
     */
    @PutMapping("/{courseId}/refresh")
    public ResponseEntity<String> refreshCoupon(
        @PathVariable("courseId") Integer courseId,
        @RequestParam("secret") String secret,
        HttpServletRequest request
    ) {
        if (refreshSecret == null || refreshSecret.isEmpty() || !refreshSecret.equals(secret)) {
            return ResponseEntity.status(401).body("Invalid refresh secret.");
        }

        courseResponseService.refreshCouponAsync(courseId, request.getRemoteAddr());
        return ResponseEntity.accepted().body("Refresh request received.");
    }

    /**
     * Updates an existing coupon.
     * <p>
     * Update functionality is currently disabled.
     *
     * @param courseId     the identifier of the coupon to update
     * @param requestBody  details of the update
     * @return the updated coupon data
     */
    @PutMapping("/{courseId}")
    public CouponDetailDTO updateCoupon(
        @PathVariable("courseId") Integer courseId,
        @RequestBody CouponUpdateRequestDTO requestBody
    ) {
        // TODO: Implement refresh from Udemy function.
        // This will update the latest data in our DB.
        // MUST include a hash check to ensure the request is valid before performing the update.
        throw new UnsupportedOperationException("Manual update is not yet implemented.");
    }

    /**
     * Retrieves coupon details for a specific course identified by courseId.
     */
    @GetMapping("/{courseId}")
    public CouponDetailDTO getCouponDetail(@PathVariable("courseId") String courseId) {
        return courseResponseService.getCouponDetail(courseId);
    }
}

