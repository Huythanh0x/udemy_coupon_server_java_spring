package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.controller;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.dto.PagedCouponResponseDTO;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.coupon.CouponCourseData;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.service.CourseResponseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "api/v1/coupons")
public class CouponCourseController {
    CourseResponseService courseResponseService;

    @Autowired
    public CouponCourseController(CourseResponseService courseResponseService) {
        this.courseResponseService = courseResponseService;
    }

    @GetMapping({"/", ""})
    @ResponseBody
    public PagedCouponResponseDTO getCoupons(@RequestParam(required = false,  defaultValue = "0") String pageIndex, @RequestParam(required = false,  defaultValue = "10") String numberPerPage, HttpServletRequest request) {
        return courseResponseService.getPagedCoupons(pageIndex, numberPerPage, request.getRemoteAddr());
    }

    @PostMapping({"/", ""})
    @ResponseBody
    public CouponCourseData postNewCouponUrl(@RequestParam String couponUrl, HttpServletRequest request) {
        return courseResponseService.saveNewCouponUrl(couponUrl, request.getRemoteAddr());
    }

    @DeleteMapping({"/", ""})
    @ResponseBody
    public void deleteCoupon(@RequestParam String couponUrl, HttpServletRequest request) {
        courseResponseService.deleteCoupon(couponUrl, request.getRemoteAddr());
    }


    @PutMapping({"/", ""})
    @ResponseBody
    public void updateCoupon(@RequestParam String couponId) {
//        TODO implement later
    }

    @GetMapping("/filter")
    public PagedCouponResponseDTO filterCoupons(@RequestParam(defaultValue = "") String category, @RequestParam(defaultValue = "-1") String rating, @RequestParam(defaultValue = "-1") String contentLength, @RequestParam(defaultValue = "") String level, @RequestParam(defaultValue = "") String language, @RequestParam(required = false,  defaultValue = "0") String pageIndex, @RequestParam(required = false,  defaultValue = "10") String numberPerPage, HttpServletRequest request) {
        return courseResponseService.filterCoupons(rating, contentLength, level, category, language, request.getRemoteAddr(), pageIndex, numberPerPage);
    }


    @GetMapping("/search")
    public PagedCouponResponseDTO searchCoupons(@RequestParam String querySearch, @RequestParam(required = false, defaultValue = "0") String pageIndex, @RequestParam(required = false,  defaultValue = "10") String numberPerPage, HttpServletRequest request) {
        return courseResponseService.searchCoupons(querySearch, pageIndex, numberPerPage, request.getRemoteAddr());
    }

    @GetMapping("{courseId}")
    public CouponCourseData getCouponDetail(@PathVariable("courseId") String courseId){
        return courseResponseService.getCouponDetail(courseId);
    }
}
