package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for a curriculum item (lecture, quiz, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumItemDTO {
    private Long id;
    private String title;
    private String description;
    private String contentSummary; // e.g., "15:47"
    private String itemType; // "lecture", "quiz", "assignment", "practice_test"
    private Boolean canBePreviewed;
    private Boolean isCodingExercise;
    private Boolean isPracticeTest;
    private String previewUrl;
    private String learnUrl;
    private Integer objectIndex;
}

