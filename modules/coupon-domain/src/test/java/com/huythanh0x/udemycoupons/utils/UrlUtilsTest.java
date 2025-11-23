package com.huythanh0x.udemycoupons.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UrlUtils class.
 */
class UrlUtilsTest {

    @Test
    void testGetCouponAPI_WithValidInputs_ReturnsCorrectUrl() {
        // Arrange
        int courseId = 12345;
        String couponCode = "ABC123";

        // Act
        String result = UrlUtils.getCouponAPI(courseId, couponCode);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(String.valueOf(courseId)));
        assertTrue(result.contains(couponCode));
        assertTrue(result.contains("udemy.com/api-2.0/course-landing-components"));
    }

    @Test
    void testGetCourseAPI_WithValidCourseId_ReturnsCorrectUrl() {
        // Arrange
        int courseId = 12345;

        // Act
        String result = UrlUtils.getCourseAPI(courseId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains(String.valueOf(courseId)));
        assertTrue(result.contains("udemy.com/api-2.0/courses"));
    }

    @Test
    void testDecodeBase64String_WithValidBase64_ReturnsDecoded() {
        // Arrange
        String original = "Hello World";
        String encoded = java.util.Base64.getEncoder().encodeToString(original.getBytes());

        // Act
        String decoded = UrlUtils.decodeBase64String(encoded);

        // Assert
        assertEquals(original, decoded);
    }

    @Test
    void testDecodeBase64String_WithNull_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            UrlUtils.decodeBase64String(null);
        });
    }
}

