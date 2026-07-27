package com.thanh0x.coursedeal.model.coupon;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter to handle flexible string-to-enum mapping for CourseLevel.
 * This prevents 500 errors when the database contains strings that don't exactly match enum names.
 */
@Converter(autoApply = true)
public class CourseLevelConverter implements AttributeConverter<CourseLevel, String> {

    @Override
    public String convertToDatabaseColumn(CourseLevel level) {
        if (level == null) return null;
        return level.name();
    }

    @Override
    public CourseLevel convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return CourseLevel.ALL_LEVELS;
        return CourseLevel.fromString(dbData);
    }
}
