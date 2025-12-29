package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for instructor response to a review.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private String content;
    private String contentHtml;
    private String created;
    private String createdFormatted; // e.g., "18 hours ago"
    private ReviewUserDTO user;
}

