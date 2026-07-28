package com.thanh0x.coursedeal.model.coupon

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
class ExpiredCourseData(
    @Id
    var couponUrl: String = "",
    /**
     * Course ID from Udemy. This allows us to reuse the courseId without making HTTP requests
     * when rechecking expired coupons, significantly reducing API calls.
     */
    @Column(name = "course_id", nullable = true)
    var courseId: Int? = null,
    /**
     * Course title. Useful for debugging and display purposes.
     */
    @Column(name = "title", nullable = true, length = 500)
    var title: String? = null,
    /**
     * Timestamp when this coupon was marked as expired (when we detected it as expired).
     * This is NOT the expiration time of the coupon itself.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    var createdAt: LocalDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    var updatedAt: LocalDateTime? = null,
) {
    // Secondary constructors for convenience
    constructor(couponUrl: String) : this(couponUrl, null, null)
    constructor(couponUrl: String, courseId: Int?) : this(couponUrl, courseId, null)
    constructor(couponUrl: String, courseId: Int?, title: String?) : this(couponUrl, courseId, title, null, null)
}
