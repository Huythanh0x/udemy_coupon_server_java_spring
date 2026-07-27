package com.thanh0x.coursedeal.dto;

import com.thanh0x.coursedeal.model.coupon.CourseLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Full DTO for coupon detail view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDetailDTO {
    private int courseId;
    private String category;
    private String subCategory;
    private String title;
    private int contentLength;
    private CourseLevel level;
    private String author;
    private float rating;
    private int reviews;
    private int students;
    private String couponCode;
    private String previewImage;
    private String couponUrl;
    private Instant expiredDate;
    private int usesRemaining;
    private String heading;
    private String description;
    private String previewVideo;
    private String language;
    private boolean isNew;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
