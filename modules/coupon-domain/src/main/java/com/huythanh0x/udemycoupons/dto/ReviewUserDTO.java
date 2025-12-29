package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for review user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUserDTO {
    private String displayName;
    private String publicDisplayName;
    private String image50x50;
    private String initials;
}

