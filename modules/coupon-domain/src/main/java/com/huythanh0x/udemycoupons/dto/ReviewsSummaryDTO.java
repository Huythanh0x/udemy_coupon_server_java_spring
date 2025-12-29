package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for reviews summary information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewsSummaryDTO {
    private Integer totalCount;
    private Float averageRating;
    private List<ReviewDTO> recentReviews; // First 3-5 reviews
}

