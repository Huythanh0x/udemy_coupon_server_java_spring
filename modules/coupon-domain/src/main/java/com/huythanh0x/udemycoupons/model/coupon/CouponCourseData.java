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

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponCourseData {
    @Id
    private int courseId;
    private String category;
    private String subCategory;
    private String title;
    private int contentLength;
    private String level;
    private String author;
    private float rating;
    private int reviews;
    private int students;
    private String couponCode;
    private String previewImage;
    private String couponUrl;
    @Column(name = "expired_date", nullable = false)
    private Instant expiredDate;
    private int usesRemaining;
    private String heading;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String previewVideo;
    private String language;
    @Builder.Default
    @Column(name = "is_new", nullable = false)
    private boolean isNew = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
