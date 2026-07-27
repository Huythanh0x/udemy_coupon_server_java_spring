package com.thanh0x.coursedeal.mapper;

import com.thanh0x.coursedeal.dto.CouponDetailDTO;
import com.thanh0x.coursedeal.dto.CouponSummaryDTO;
import com.thanh0x.coursedeal.model.coupon.CouponCourseData;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CouponMapperTest {

    private final CouponMapper mapper = new CouponMapper();

    @Test
    void toSummaryDto_ShouldMapCorrectly() {
        CouponCourseData entity = CouponCourseData.builder()
                .courseId(123)
                .title("Test Course")
                .category("Development")
                .author("Author Name")
                .rating(4.5f)
                .students(1000)
                .previewImage("image.jpg")
                .couponUrl("http://udemy.com/test")
                .expiredDate(Instant.now())
                .usesRemaining(50)
                .isNew(true)
                .description("Long description that should be hidden")
                .build();

        CouponSummaryDTO dto = mapper.toSummaryDto(entity);

        assertEquals(entity.getCourseId(), dto.getCourseId());
        assertEquals(entity.getTitle(), dto.getTitle());
        assertEquals(entity.getCategory(), dto.getCategory());
        assertEquals(entity.getRating(), dto.getRating());
        // Verify sensitive or heavy fields are NOT in summary DTO if we didn't add them
        // (Currently CouponSummaryDTO does not have description)
    }

    @Test
    void toDetailDto_ShouldIncludeAllFields() {
        CouponCourseData entity = CouponCourseData.builder()
                .courseId(123)
                .title("Test Course")
                .description("Detailed description")
                .previewVideo("video.mp4")
                .build();

        CouponDetailDTO dto = mapper.toDetailDto(entity);

        assertEquals(entity.getCourseId(), dto.getCourseId());
        assertEquals(entity.getDescription(), dto.getDescription());
        assertEquals(entity.getPreviewVideo(), dto.getPreviewVideo());
    }

    @Test
    void toSummaryDto_ShouldReturnNullWhenEntityIsNull() {
        assertNull(mapper.toSummaryDto(null));
    }
}
