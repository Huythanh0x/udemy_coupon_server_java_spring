package com.thanh0x.coursedeal.dto

/**
 * DTO for course incentives (certificate, lifetime access, etc.).
 */
data class IncentivesDTO(
    val videoContentLength: String? = null, // e.g., "2.5 hours"
    val numArticles: Int? = null,
    val numQuizzes: Int? = null,
    val numPracticeTests: Int? = null,
    val numCodingExercises: Int? = null,
    val hasLifetimeAccess: Boolean? = null,
    val devicesAccess: String? = null, // e.g., "Access on mobile and TV"
    val hasAssignments: Boolean? = null,
    val hasCertificate: Boolean? = null,
    val hasClosedCaptions: Boolean? = null
)
