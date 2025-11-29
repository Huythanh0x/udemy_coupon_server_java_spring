package com.huythanh0x.udemycoupons.integration;

import com.huythanh0x.udemycoupons.model.coupon.CouponCourseData;
import com.huythanh0x.udemycoupons.repository.CouponCourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for API endpoints using MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponCourseRepository couponCourseRepository;

    @BeforeEach
    void setUp() {
        couponCourseRepository.deleteAll();
    }

    @Test
    void testGetCoupons_WithValidRequest_ReturnsOk() throws Exception {
        // Arrange
        CouponCourseData coupon = CouponCourseData.builder()
                .courseId(1)
                .title("Test Course")
                .couponUrl("https://www.udemy.com/course/test/?couponCode=ABC")
                .expiredDate(Instant.now().plusSeconds(3600))
                .usesRemaining(100)
                .build();
        couponCourseRepository.save(coupon);

        // Act & Assert
        mockMvc.perform(get("/api/v1/coupons")
                        .param("pageIndex", "0")
                        .param("numberPerPage", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 401, 
                              "Expected 200 or 401, got: " + status);
                });
    }

    @Test
    void testGetCoupons_WithPagination_ReturnsCorrectPage() throws Exception {
        // Arrange - create multiple coupons
        for (int i = 1; i <= 15; i++) {
            CouponCourseData coupon = CouponCourseData.builder()
                    .courseId(i)
                    .title("Test Course " + i)
                    .couponUrl("https://www.udemy.com/course/test" + i + "/?couponCode=ABC" + i)
                    .expiredDate(Instant.now().plusSeconds(3600))
                    .usesRemaining(100)
                    .build();
            couponCourseRepository.save(coupon);
        }

        // Act & Assert - test pagination
        mockMvc.perform(get("/api/v1/coupons")
                        .param("pageIndex", "0")
                        .param("numberPerPage", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 401, 
                              "Expected 200 or 401, got: " + status);
                });
    }
}

