package com.thanh0x.coursedeal.model.coupon

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
class CouponCourseHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var courseId: Int? = null,
    @Column(length = 500)
    var title: String? = null,
    @Column(nullable = false)
    var couponUrl: String = "",
    @Column(nullable = false, length = 32)
    var status: String = "",
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    var createdAt: LocalDateTime? = null,
)
