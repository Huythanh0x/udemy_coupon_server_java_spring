package com.thanh0x.coursedeal.controller

import com.thanh0x.coursedeal.dto.CouponDetailDTO
import com.thanh0x.coursedeal.dto.CouponRequestDTO
import com.thanh0x.coursedeal.dto.CouponUpdateRequestDTO
import com.thanh0x.coursedeal.dto.PagedCouponResponseDTO
import com.thanh0x.coursedeal.service.CourseResponseService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller class for managing coupon-related endpoints.
 */
@RestController
@RequestMapping("api/v1/coupons")
class CouponCourseController(private val courseResponseService: CourseResponseService) {

    /**
     * Retrieves a paged list of coupons with optional filtering, search, and sorting criteria.
     */
    @GetMapping
    fun listCoupons(
        @RequestParam(required = false, defaultValue = "") category: String,
        @RequestParam(required = false, defaultValue = "-1") rating: String,
        @RequestParam(required = false, defaultValue = "-1") contentLength: String,
        @RequestParam(required = false, defaultValue = "") level: String,
        @RequestParam(required = false, defaultValue = "") language: String,
        @RequestParam(required = false, defaultValue = "") query: String,
        @RequestParam(required = false, defaultValue = "createdAt") sortBy: String,
        @RequestParam(required = false, defaultValue = "desc") sortOrder: String,
        @RequestParam(required = false, defaultValue = "0") pageIndex: String,
        @RequestParam(required = false, defaultValue = "10") numberPerPage: String,
        request: HttpServletRequest
    ): PagedCouponResponseDTO {
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
            request.remoteAddr
        )
    }

    /**
     * Creates a new coupon based on the provided Udemy coupon URL.
     * Returns 202 Accepted as validation is performed asynchronously.
     */
    @PostMapping
    fun createCoupon(
        @Valid @RequestBody requestBody: CouponRequestDTO,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        courseResponseService.saveNewCouponUrlAsync(requestBody.couponUrl!!, request.remoteAddr)
        return ResponseEntity.accepted().body("Coupon submission received and is being processed.")
    }

    /**
     * Deletes a coupon by its course identifier.
     * <p>
     * Direct deletion is currently disabled.
     *
     * @param courseId the course identifier of the coupon to be deleted
     */
    @DeleteMapping("/{courseId}")
    fun deleteCoupon(@PathVariable("courseId") courseId: Int) {
        throw UnsupportedOperationException("Direct deletion is not allowed.")
    }

    @Value("\${custom.refresh-secret:}")
    private var refreshSecret: String? = null

    /**
     * Triggers a refresh of the coupon data from Udemy.
     * Requires a valid hash/secret to prevent unauthorized scraping load.
     */
    @PutMapping("/{courseId}/refresh")
    fun refreshCoupon(
        @PathVariable("courseId") courseId: Int,
        @RequestParam("secret") secret: String,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        if (refreshSecret.isNullOrEmpty() || refreshSecret != secret) {
            return ResponseEntity.status(401).body("Invalid refresh secret.")
        }

        courseResponseService.refreshCouponAsync(courseId, request.remoteAddr)
        return ResponseEntity.accepted().body("Refresh request received.")
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
    fun updateCoupon(
        @PathVariable("courseId") courseId: Int,
        @RequestBody requestBody: CouponUpdateRequestDTO
    ): CouponDetailDTO {
        // TODO: Implement refresh from Udemy function.
        // This will update the latest data in our DB.
        // MUST include a hash check to ensure the request is valid before performing the update.
        throw UnsupportedOperationException("Manual update is not yet implemented.")
    }

    /**
     * Retrieves coupon details for a specific course identified by courseId.
     */
    @GetMapping("/{courseId}")
    fun getCouponDetail(@PathVariable("courseId") courseId: String): CouponDetailDTO {
        return courseResponseService.getCouponDetail(courseId)
    }
}
