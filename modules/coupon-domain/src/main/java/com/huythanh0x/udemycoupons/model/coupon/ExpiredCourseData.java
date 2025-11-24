package com.huythanh0x.udemycoupons.model.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpiredCourseData {
    @Id
    String couponUrl;
    
    /**
     * Course ID from Udemy. This allows us to reuse the courseId without making HTTP requests
     * when rechecking expired coupons, significantly reducing API calls.
     */
    @Column(name = "course_id", nullable = true)
    private Integer courseId;
    
    /**
     * Course title. Useful for debugging and display purposes.
     */
    @Column(name = "title", nullable = true, length = 500)
    private String title;
    
    /**
     * Timestamp when this coupon was marked as expired (when we detected it as expired).
     * This is NOT the expiration time of the coupon itself.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    /**
     * Constructor with just URL (for backward compatibility and when courseId is unknown)
     */
    public ExpiredCourseData(String couponUrl) {
        this.couponUrl = couponUrl;
    }

    /**
     * Constructor with URL and courseId
     */
    public ExpiredCourseData(String couponUrl, Integer courseId) {
        this.couponUrl = couponUrl;
        this.courseId = courseId;
    }

    /**
     * Constructor with URL, courseId, and title
     */
    public ExpiredCourseData(String couponUrl, Integer courseId, String title) {
        this.couponUrl = couponUrl;
        this.courseId = courseId;
        this.title = title;
    }
}
