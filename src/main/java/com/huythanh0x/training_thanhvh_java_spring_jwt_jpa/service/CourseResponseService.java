package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.service;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.UdemyCouponCourseExtractor;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.dto.PagedCouponResponseDTO;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.exception.BadRequestException;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.coupon.CouponCourseData;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository.CouponCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CourseResponseService {
    private final CouponCourseRepository couponCourseRepository;


    @Autowired
    public CourseResponseService(CouponCourseRepository couponCourseRepository) {
        this.couponCourseRepository = couponCourseRepository;
    }

    public PagedCouponResponseDTO getPagedCoupons(String pageIndex, String numberPerPage, String remoteAddr) {
        handlePagingParameters(pageIndex, numberPerPage);
        Pageable pageable = PageRequest.of(Integer.parseInt(pageIndex), Integer.parseInt(numberPerPage));
        Page<CouponCourseData> allCouponCourses = couponCourseRepository.findAll(pageable);
        return new PagedCouponResponseDTO(allCouponCourses);
    }

    public PagedCouponResponseDTO filterCoupons(String rating, String contentLength, String level, String category, String language, String pageIndex, String numberPerPage, String remoteAddr) {
        handlePagingParameters(pageIndex, numberPerPage);
        Pageable pageable = PageRequest.of(Integer.parseInt(pageIndex), Integer.parseInt(numberPerPage));
        Page<CouponCourseData> filterCouponCourses = couponCourseRepository.findByRatingGreaterThanAndContentLengthGreaterThanAndLevelContainingAndCategoryIsContainingIgnoreCaseAndLanguageContaining(Float.parseFloat(rating), Integer.parseInt(contentLength), level, category, language, pageable);
        return new PagedCouponResponseDTO(filterCouponCourses);
    }

    public PagedCouponResponseDTO searchCoupons(String querySearch, String pageIndex, String numberPerPage, String remoteAddr) {
        handlePagingParameters(pageIndex, numberPerPage);
        Pageable pageable = PageRequest.of(Integer.parseInt(pageIndex), Integer.parseInt(numberPerPage));
        Page<CouponCourseData> searchedCouponCourses = couponCourseRepository.findByTitleContainingOrDescriptionContainingOrHeadingContaining(querySearch, querySearch, querySearch, pageable);
        return new PagedCouponResponseDTO(searchedCouponCourses);
    }

    public CouponCourseData saveNewCouponUrl(String couponUrl, String remoteAddr) {
        return couponCourseRepository.save(new UdemyCouponCourseExtractor(couponUrl).getFullCouponCodeData());
    }

    public void deleteCoupon(String couponUrl, String remoteAddr) {
        if (new UdemyCouponCourseExtractor(couponUrl).getFullCouponCodeData() == null) {
            couponCourseRepository.deleteByCouponUrl(couponUrl);
        }
    }

    public void handlePagingParameters(String pageIndex, String numberPerPage) {
        try {
            Integer.parseInt(pageIndex);
            Integer.parseInt(numberPerPage);
        } catch (Exception e) {
            throw new BadRequestException(e.toString());
        }
        if (Integer.parseInt(pageIndex) < 0 || Integer.parseInt(numberPerPage) < 0) {
            throw new BadRequestException("Page index and number of course per page cannot be negative");
        }
    }

    public CouponCourseData getCouponDetail(String courseId) {
        CouponCourseData couponCourseData = couponCourseRepository.findByCourseId(Integer.parseInt(courseId));
        if (couponCourseData != null) {
            return couponCourseData;
        } else {
            throw new BadRequestException("Course id not found");
        }
    }
}
