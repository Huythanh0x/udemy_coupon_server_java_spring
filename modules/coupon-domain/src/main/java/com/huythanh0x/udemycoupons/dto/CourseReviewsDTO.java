package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for paginated course reviews response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseReviewsDTO {
    private List<ReviewDTO> reviews;
    private Integer totalCount;
    private Integer currentPage;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private String nextUrl;
    private String previousUrl;
}

