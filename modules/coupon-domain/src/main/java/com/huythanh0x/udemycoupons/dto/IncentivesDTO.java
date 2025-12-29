package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for course incentives (certificate, lifetime access, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncentivesDTO {
    private String videoContentLength; // e.g., "2.5 hours"
    private Integer numArticles;
    private Integer numQuizzes;
    private Integer numPracticeTests;
    private Integer numCodingExercises;
    private Boolean hasLifetimeAccess;
    private String devicesAccess; // e.g., "Access on mobile and TV"
    private Boolean hasAssignments;
    private Boolean hasCertificate;
    private Boolean hasClosedCaptions;
}

