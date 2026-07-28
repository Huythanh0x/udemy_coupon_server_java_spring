package com.thanh0x.coursedeal.controller

import com.thanh0x.coursedeal.config.ApiProperties
import com.thanh0x.coursedeal.dto.CouponDetailDTO
import com.thanh0x.coursedeal.dto.CouponQueryDTO
import com.thanh0x.coursedeal.dto.CouponRequestDTO
import com.thanh0x.coursedeal.dto.CouponUpdateRequestDTO
import com.thanh0x.coursedeal.dto.PagedCouponResponseDTO
import com.thanh0x.coursedeal.service.CourseResponseService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Controller class for managing coupon-related endpoints.
 */
@RestController
@RequestMapping("api/v1/coupons")
class CouponCourseController(
    private val courseResponseService: CourseResponseService,
    private val properties: ApiProperties,
) {
    @GetMapping
    fun listCoupons(
        @ModelAttribute queryDto: CouponQueryDTO,
        request: HttpServletRequest,
    ): PagedCouponResponseDTO {
        return courseResponseService.listCoupons(queryDto, request.remoteAddr)
    }

    /**
     * Creates a new coupon based on the provided Udemy coupon URL.
     * Returns 202 Accepted as validation is performed asynchronously.
     */
    @PostMapping
    fun createCoupon(
        @Valid @RequestBody requestBody: CouponRequestDTO,
        request: HttpServletRequest,
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
    fun deleteCoupon(
        @PathVariable("courseId") courseId: Int,
    ) {
        throw UnsupportedOperationException("Direct deletion is not allowed.")
    }

    /**
     * Triggers a refresh of the coupon data from Udemy.
     * Requires a valid hash/secret to prevent unauthorized scraping load.
     */
    @PutMapping("/{courseId}/refresh")
    fun refreshCoupon(
        @PathVariable("courseId") courseId: Int,
        @RequestParam("secret") secret: String,
        request: HttpServletRequest,
    ): ResponseEntity<String> {
        if (properties.refreshSecret.isEmpty() || properties.refreshSecret != secret) {
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
        @RequestBody requestBody: CouponUpdateRequestDTO,
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
    fun getCouponDetail(
        @PathVariable("courseId") courseId: String,
    ): CouponDetailDTO {
        return courseResponseService.getCouponDetail(courseId)
    }
}
