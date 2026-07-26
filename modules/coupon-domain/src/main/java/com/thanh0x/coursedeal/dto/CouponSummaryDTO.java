package com.thanh0x.coursedeal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Optimized DTO for coupon list view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponSummaryDTO {
    private int courseId;
    private String title;
    private String category;
    private String author;
    private float rating;
    private int students;
    private String previewImage;
    private String couponUrl;
    private Instant expiredDate;
    private int usesRemaining;
    private boolean isNew;
}
