package com.thanh0x.coursedeal.dto

/**
 * DTO for a curriculum section.
 */
data class CurriculumSectionDTO(
    val title: String? = null,
    val index: Int? = null,
    val duration: String? = null, // e.g., "15:47"
    val durationSeconds: Int? = null,
    val lectureCount: Int? = null,
    val items: List<CurriculumItemDTO>? = null,
)
