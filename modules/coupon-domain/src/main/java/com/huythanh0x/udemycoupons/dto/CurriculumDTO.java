package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for course curriculum/syllabus.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumDTO {
    private List<CurriculumSectionDTO> sections;
    private String totalDuration; // e.g., "2.5 hours"
    private Integer totalDurationSeconds;
    private Integer totalLectures;
}

