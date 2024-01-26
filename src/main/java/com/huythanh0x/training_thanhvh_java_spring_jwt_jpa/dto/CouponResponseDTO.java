package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.dto;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.coupon.CouponCourseData;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.utils.LastFetchTimeManager;

import java.time.LocalDateTime;
import java.util.List;

public class CouponResponseDTO {
    LocalDateTime lastFetchTime;
    Integer couponCount;
    List<CouponCourseData> courses;

    public CouponResponseDTO(List<CouponCourseData> courses) {
        this.courses = courses;
        this.couponCount = courses.size();
        this.lastFetchTime = LastFetchTimeManager.loadLasFetchedTimeInDateTimeString();
    }

    public Integer getCouponCount() {
        return couponCount;
    }

    public LocalDateTime getLastFetchTime() {
        return lastFetchTime;
    }

    public List<CouponCourseData> getCourses() {
        return courses;
    }
}
