package com.thanh0x.coursedeal.dto

/**
 * DTO for course curriculum/syllabus.
 */
data class CurriculumDTO(
    val sections: List<CurriculumSectionDTO>? = null,
    val totalDuration: String? = null, // e.g., "2.5 hours"
    val totalDurationSeconds: Int? = null,
    val totalLectures: Int? = null,
)
