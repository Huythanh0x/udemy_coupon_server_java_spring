package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for a curriculum section.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumSectionDTO {
    private String title;
    private Integer index;
    private String duration; // e.g., "15:47"
    private Integer durationSeconds;
    private Integer lectureCount;
    private List<CurriculumItemDTO> items;
}

