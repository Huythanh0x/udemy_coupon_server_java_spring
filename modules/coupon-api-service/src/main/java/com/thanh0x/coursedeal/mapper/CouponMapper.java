package com.thanh0x.coursedeal.mapper;

import com.thanh0x.coursedeal.dto.CouponDetailDTO;
import com.thanh0x.coursedeal.dto.CouponSummaryDTO;
import com.thanh0x.coursedeal.model.coupon.CouponCourseData;
import org.springframework.stereotype.Component;

@Component
public class CouponMapper {

    public CouponSummaryDTO toSummaryDto(CouponCourseData entity) {
        if (entity == null) return null;
        return CouponSummaryDTO.builder()
                .courseId(entity.getCourseId())
                .title(entity.getTitle())
                .category(entity.getCategory())
                .author(entity.getAuthor())
                .rating(entity.getRating())
                .students(entity.getStudents())
                .previewImage(entity.getPreviewImage())
                .couponUrl(entity.getCouponUrl())
                .expiredDate(entity.getExpiredDate())
                .usesRemaining(entity.getUsesRemaining())
                .isNew(entity.isNew())
                .build();
    }

    public CouponDetailDTO toDetailDto(CouponCourseData entity) {
        if (entity == null) return null;
        return CouponDetailDTO.builder()
                .courseId(entity.getCourseId())
                .category(entity.getCategory())
                .subCategory(entity.getSubCategory())
                .title(entity.getTitle())
                .contentLength(entity.getContentLength())
                .level(entity.getLevel())
                .author(entity.getAuthor())
                .rating(entity.getRating())
                .reviews(entity.getReviews())
                .students(entity.getStudents())
                .couponCode(entity.getCouponCode())
                .previewImage(entity.getPreviewImage())
                .couponUrl(entity.getCouponUrl())
                .expiredDate(entity.getExpiredDate())
                .usesRemaining(entity.getUsesRemaining())
                .heading(entity.getHeading())
                .description(entity.getDescription())
                .previewVideo(entity.getPreviewVideo())
                .language(entity.getLanguage())
                .isNew(entity.isNew())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
