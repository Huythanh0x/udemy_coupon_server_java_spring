package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.controller;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.dto.PagedCouponResponseDTO;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.coupon.CouponCourseData;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.service.CourseResponseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for managing coupons related endpoints
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "api/v1/coupons")
public class CouponCourseController {
    CourseResponseService courseResponseService;

    @Autowired
    public CouponCourseController(CourseResponseService courseResponseService) {
        this.courseResponseService = courseResponseService;
    }

    /**
     * Retrieves a paged list of coupons based on the specified page index and number of coupons per page.
     * If no parameters are provided, defaults to returning the first page with 10 coupons.
     *
     * @param pageIndex The index of the page to retrieve. Defaults to 0 if not provided.
     * @param numberPerPage The number of coupons to display per page. Defaults to 10 if not provided.
     * @param request The HTTP servlet request containing client information.
     * @return A PagedCouponResponseDTO object containing the paged list of coupons.
     */
    @GetMapping({"/", ""})
    @ResponseBody
    public PagedCouponResponseDTO getCoupons(@RequestParam(required = false, defaultValue = "0") String pageIndex, @RequestParam(required = false, defaultValue = "10") String numberPerPage, HttpServletRequest request) {
        return courseResponseService.getPagedCoupons(pageIndex, numberPerPage, request.getRemoteAddr());
    }

    /**
     * Post method for creating a new coupon url.
     *
     * @param couponUrl the url of the coupon being created
     * @param request the HTTP servlet request
     * @return the saved course data with the new coupon url
     */
    @PostMapping({"/", ""})
    @ResponseBody
    public CouponCourseData postNewCouponUrl(@RequestParam String couponUrl, HttpServletRequest request) {
        return courseResponseService.saveNewCouponUrl(couponUrl, request.getRemoteAddr());
    }

    /**
     * Handles HTTP DELETE requests to delete a coupon.
     * @param couponUrl the URL of the coupon to be deleted
     * @param request the HTTP request
     */
    @DeleteMapping({"/", ""})
    @ResponseBody
    public void deleteCoupon(@RequestParam String couponUrl, HttpServletRequest request) {
        courseResponseService.deleteCoupon(couponUrl, request.getRemoteAddr());
    }


    /**
     * Update the coupon with the given coupon ID.
     * @param couponId The unique identifier of the coupon to be updated.
     */
    @PutMapping({"/", ""})
    @ResponseBody
    public void updateCoupon(@RequestParam String couponId) {
//        TODO implement later
    }

    /**
     * Retrieves a paginated list of coupons based on the specified filters.
     *
     * @param category the category of the coupons to filter by (default empty string)
     * @param rating the minimum rating of the coupons to filter by (default -1)
     * @param contentLength the content length of the coupons to filter by (default -1)
     * @param level the level of the coupons to filter by (default empty string)
     * @param language the language of the coupons to filter by (default empty string)
     * @param pageIndex the index of the page to retrieve (default 0)
     * @param numberPerPage the number of items per page (default 10)
     * @param request the HttpServletRequest containing the request information
     * @return a PaginatedCouponResponseDTO object containing the paginated list of filtered coupons
     */
    @GetMapping("/filter")
    public PagedCouponResponseDTO filterCoupons(@RequestParam(defaultValue = "") String category, @RequestParam(defaultValue = "-1") String rating, @RequestParam(defaultValue = "-1") String contentLength, @RequestParam(defaultValue = "") String level, @RequestParam(defaultValue = "") String language, @RequestParam(required = false, defaultValue = "0") String pageIndex, @RequestParam(required = false, defaultValue = "10") String numberPerPage, HttpServletRequest request) {
        return courseResponseService.filterCoupons(rating, contentLength, level, category, language, pageIndex, numberPerPage, request.getRemoteAddr());
    }


    /**
     * Retrieves a paginated list of coupons based on the given search query.
     *
     * @param querySearch The search query used to filter coupons.
     * @param pageIndex The page index of the paginated results (default: 0).
     * @param numberPerPage The number of coupons per page (default: 10).
     * @param request The HTTP servlet request.
     * @return A PagedCouponResponseDTO object containing the paginated list of coupons.
     */
    @GetMapping("/search")
    public PagedCouponResponseDTO searchCoupons(@RequestParam String querySearch, @RequestParam(required = false, defaultValue = "0") String pageIndex, @RequestParam(required = false, defaultValue = "10") String numberPerPage, HttpServletRequest request) {
        return courseResponseService.searchCoupons(querySearch, pageIndex, numberPerPage, request.getRemoteAddr());
    }

    /**
     * Retrieves coupon details for a specific course identified by courseId.
     *
     * @param courseId The unique identifier of the course.
     * @return The CouponCourseData object containing details of the course's coupon.
     */
    @GetMapping("{courseId}")
    public CouponCourseData getCouponDetail(@PathVariable("courseId") String courseId) {
        return courseResponseService.getCouponDetail(courseId);
    }
}
