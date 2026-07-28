package com.thanh0x.coursedeal.model.coupon

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * JPA Converter to handle flexible string-to-enum mapping for CourseLevel.
 * This prevents 500 errors when the database contains strings that don't exactly match enum names.
 */
@Converter(autoApply = true)
class CourseLevelConverter : AttributeConverter<CourseLevel, String> {

    override fun convertToDatabaseColumn(level: CourseLevel?): String? {
        return level?.name
    }

    override fun convertToEntityAttribute(dbData: String?): CourseLevel {
        if (dbData.isNullOrBlank()) return CourseLevel.ALL_LEVELS
        return CourseLevel.fromString(dbData)
    }
}
