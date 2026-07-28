package com.thanh0x.coursedeal.model.coupon

/**
 * Enum representing the difficulty level of a course.
 */
enum class CourseLevel(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    EXPERT("Expert"),
    ALL_LEVELS("All Levels"),
    ;

    companion object {
        fun fromString(text: String?): CourseLevel {
            if (text == null) return ALL_LEVELS
            return entries.find {
                it.displayName.equals(text, ignoreCase = true) || it.name.equals(text, ignoreCase = true)
            } ?: ALL_LEVELS
        }
    }
}
