package com.huythanh0x.udemycoupons.dto;

import com.huythanh0x.udemycoupons.model.coupon.CouponCourseData;
import com.huythanh0x.udemycoupons.utils.LastFetchTimeManager;

import java.util.List;

public class CouponResponseDTO {
    Long lastFetchTime; // Epoch milliseconds
    Integer couponCount;
    List<CouponCourseData> courses;

    public CouponResponseDTO(List<CouponCourseData> courses) {
        this.courses = courses;
        this.couponCount = courses.size();
        this.lastFetchTime = LastFetchTimeManager.loadLasFetchedTimeInMilliSecond();
    }

    public Integer getCouponCount() {
        return couponCount;
    }

    public Long getLastFetchTime() {
        return lastFetchTime;
    }

    public List<CouponCourseData> getCourses() {
        return courses;
    }
}
