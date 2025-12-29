package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for individual course review.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private String content;
    private String contentHtml;
    private Float rating;
    private String created;
    private String createdFormatted; // e.g., "4 days ago"
    private ReviewUserDTO user;
    private ReviewResponseDTO response; // Instructor response, if any
}

