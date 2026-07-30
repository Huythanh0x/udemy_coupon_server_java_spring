package com.thanh0x.coursedeal.dto

/**
 * DTO for a curriculum item (lecture, quiz, etc.).
 */
data class CurriculumItemDTO(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    // e.g., "15:47"
    val contentSummary: String? = null,
    // "lecture", "quiz", "assignment", "practice_test"
    val itemType: String? = null,
    val canBePreviewed: Boolean? = null,
    val isCodingExercise: Boolean? = null,
    val isPracticeTest: Boolean? = null,
    val previewUrl: String? = null,
    val learnUrl: String? = null,
    val objectIndex: Int? = null,
)
