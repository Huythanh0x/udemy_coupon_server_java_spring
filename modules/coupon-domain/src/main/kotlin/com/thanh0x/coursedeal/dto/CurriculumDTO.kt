package com.thanh0x.coursedeal.dto

/**
 * DTO for course curriculum/syllabus.
 */
data class CurriculumDTO(
    val sections: List<CurriculumSectionDTO>? = null,
    // e.g., "2.5 hours"
    val totalDuration: String? = null,
    val totalDurationSeconds: Int? = null,
    val totalLectures: Int? = null,
)
