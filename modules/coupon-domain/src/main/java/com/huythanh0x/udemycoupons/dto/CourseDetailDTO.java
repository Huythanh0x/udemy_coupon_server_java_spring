package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for comprehensive course details including reviews summary, curriculum, and related courses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailDTO {
    // Basic course information (from existing CouponCourseData)
    private Integer courseId;
    private String title;
    private String heading;
    private String description;
    private String author;
    private String category;
    private String subCategory;
    private String level;
    private String language;
    private Float rating;
    private Integer reviews;
    private Integer students;
    private Integer contentLength;
    private String previewImage;
    private String previewVideo;
    private String couponUrl;
    private String couponCode;
    private Integer usesRemaining;
    private java.time.Instant expiredDate;
    
    // Additional details from Udemy API
    private ReviewsSummaryDTO reviewsSummary;
    private CurriculumDTO curriculum;
    private List<RelatedCourseDTO> relatedCourses;
    private PricingInfoDTO pricingInfo;
    private IncentivesDTO incentives;
    private List<PreviewVideoDTO> previewVideos; // List of preview videos from preview page
}

