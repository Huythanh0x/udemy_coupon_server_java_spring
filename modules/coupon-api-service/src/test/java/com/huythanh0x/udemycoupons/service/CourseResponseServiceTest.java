package com.huythanh0x.udemycoupons.service;

import com.huythanh0x.udemycoupons.dto.PagedCouponResponseDTO;
import com.huythanh0x.udemycoupons.exception.BadRequestException;
import com.huythanh0x.udemycoupons.model.coupon.CouponCourseData;
import com.huythanh0x.udemycoupons.repository.CouponCourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseResponseService.
 */
@ExtendWith(MockitoExtension.class)
class CourseResponseServiceTest {

    @Mock
    private CouponCourseRepository couponCourseRepository;

    @InjectMocks
    private CourseResponseService courseResponseService;

    @BeforeEach
    void setUp() {
        // Setup can be done here if needed
    }

    @Test
    void testGetPagedCoupons_WithValidParameters_ReturnsPagedResponse() {
        // Arrange
        List<CouponCourseData> coupons = new ArrayList<>();
        Page<CouponCourseData> page = new PageImpl<>(coupons, PageRequest.of(0, 10), 0);
        when(couponCourseRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        PagedCouponResponseDTO result = courseResponseService.getPagedCoupons("0", "10", "127.0.0.1");

        // Assert
        assertNotNull(result);
        verify(couponCourseRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testHandlePagingParameters_WithInvalidPageIndex_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            courseResponseService.handlePagingParameters("invalid", "10");
        });
    }

    @Test
    void testHandlePagingParameters_WithNegativePageIndex_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            courseResponseService.handlePagingParameters("-1", "10");
        });
    }

    @Test
    void testHandlePagingParameters_WithNegativeNumberPerPage_ThrowsException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            courseResponseService.handlePagingParameters("0", "-1");
        });
    }

    @Test
    void testHandlePagingParameters_WithValidParameters_DoesNotThrow() {
        // Act & Assert - should not throw
        assertDoesNotThrow(() -> {
            courseResponseService.handlePagingParameters("0", "10");
        });
    }

    @Test
    void testGetCouponDetail_WithValidCourseId_ReturnsCoupon() {
        // Arrange
        CouponCourseData coupon = new CouponCourseData();
        coupon.setCourseId(123);
        when(couponCourseRepository.findByCourseId(123)).thenReturn(coupon);

        // Act
        CouponCourseData result = courseResponseService.getCouponDetail("123");

        // Assert
        assertNotNull(result);
        assertEquals(123, result.getCourseId());
    }

    @Test
    void testGetCouponDetail_WithInvalidCourseId_ThrowsException() {
        // Arrange
        when(couponCourseRepository.findByCourseId(anyInt())).thenReturn(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            courseResponseService.getCouponDetail("999");
        });
    }
}

