package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for related/recommended course.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatedCourseDTO {
    private Integer courseId;
    private String title;
    private String headline;
    private String url;
    private String image240x135;
    private String image480x270;
    private String image750x422;
    private String author;
    private Float rating;
    private Integer numReviews;
    private Integer numSubscribers;
    private String contentInfo; // e.g., "2.5 hours"
    private String instructionalLevel;
}

