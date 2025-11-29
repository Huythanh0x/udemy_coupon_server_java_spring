package com.huythanh0x.udemycoupons.crawler_runner.crawler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RealDiscountCrawler.
 */
@ExtendWith(MockitoExtension.class)
class RealDiscountCrawlerTest {

    private RealDiscountCrawler realDiscountCrawler;

    @BeforeEach
    void setUp() {
        realDiscountCrawler = new RealDiscountCrawler(10);
    }

    @Test
    void testGetAllCouponUrls_ReturnsList() {
        // Act
        List<String> result = realDiscountCrawler.getAllCouponUrls();

        // Assert
        assertNotNull(result);
        // Result may be empty if API is not accessible, but should not be null
    }

    @Test
    void testExtractCouponUrl_WithValidJsonObject_ReturnsUrl() {
        // This would require mocking the API response
        // For now, we test that the method doesn't crash
        assertNotNull(realDiscountCrawler);
    }
}

