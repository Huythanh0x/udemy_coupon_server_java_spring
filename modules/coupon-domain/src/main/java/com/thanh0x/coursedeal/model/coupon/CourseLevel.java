package com.thanh0x.coursedeal.model.coupon;

import lombok.Getter;

/**
 * Enum representing the difficulty level of a course.
 */
@Getter
public enum CourseLevel {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    EXPERT("Expert"),
    ALL_LEVELS("All Levels");

    private final String displayName;

    CourseLevel(String displayName) {
        this.displayName = displayName;
    }

    public static CourseLevel fromString(String text) {
        for (CourseLevel level : CourseLevel.values()) {
            if (level.displayName.equalsIgnoreCase(text) || level.name().equalsIgnoreCase(text)) {
                return level;
            }
        }
        return ALL_LEVELS; // Default
    }
}
