package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.dto;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.coupon.CouponCourseData;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.utils.LastFetchTimeManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedCouponResponseDTO {
    LocalDateTime lastFetchTime;
    Long totalCoupon;
    Integer totalPage;
    Integer currentPage;
    List<CouponCourseData> courses;

    public PagedCouponResponseDTO(Page<CouponCourseData> courses) {
        this.totalPage = courses.getTotalPages();
        this.totalCoupon = courses.getTotalElements();
        this.currentPage = courses.getPageable().getPageNumber();
        this.courses = courses.getContent();
        this.lastFetchTime = LastFetchTimeManager.loadLasFetchedTimeInDateTimeString();
    }
}
