package com.thanh0x.coursedeal.model.coupon

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Suppress("LongParameterList")
class CouponCourseData(
    @Id
    var courseId: Int = 0,
    var category: String? = null,
    var subCategory: String? = null,
    var title: String? = null,
    var contentLength: Int = 0,
    var level: CourseLevel? = null,
    var author: String? = null,
    var rating: Float = 0f,
    var reviews: Int = 0,
    var students: Int = 0,
    var couponCode: String? = null,
    var previewImage: String? = null,
    var couponUrl: String? = null,
    @Column(name = "expired_date")
    var expiredDate: Instant? = null,
    var usesRemaining: Int = 0,
    var heading: String? = null,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    var previewVideo: String? = null,
    var language: String? = null,
    @Column(name = "is_new", nullable = false)
    var isNew: Boolean = true,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null,
)
