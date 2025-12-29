package com.huythanh0x.udemycoupons.controller;

import com.huythanh0x.udemycoupons.dto.*;
import com.huythanh0x.udemycoupons.service.CourseDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for course detail endpoints.
 * Provides comprehensive course information including reviews, curriculum, and related courses.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/coupons")
@Tag(name = "Course Details", description = "API endpoints for detailed course information")
public class CourseDetailController {
    
    @Autowired
    private CourseDetailService courseDetailService;
    
    /**
     * Gets comprehensive course details including reviews summary, curriculum, and related courses.
     *
     * @param courseId the course ID
     * @param couponCode optional coupon code
     * @return CourseDetailDTO with all course information
     */
    @GetMapping("/{courseId}/details")
    @Operation(summary = "Get comprehensive course details", 
               description = "Returns detailed course information including reviews, curriculum, pricing, and related courses")
    public ResponseEntity<CourseDetailDTO> getCourseDetails(
            @Parameter(description = "Course ID", required = true)
            @PathVariable Integer courseId,
            @Parameter(description = "Optional coupon code")
            @RequestParam(required = false) String couponCode
    ) {
        CourseDetailDTO details = courseDetailService.getCourseDetails(courseId, couponCode);
        if (details == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(details);
    }
    
    /**
     * Gets paginated course reviews.
     *
     * @param courseId the course ID
     * @param page the page number (default: 1)
     * @return CourseReviewsDTO with paginated reviews
     */
    @GetMapping("/{courseId}/reviews")
    @Operation(summary = "Get course reviews", 
               description = "Returns paginated course reviews with instructor responses")
    public ResponseEntity<CourseReviewsDTO> getCourseReviews(
            @Parameter(description = "Course ID", required = true)
            @PathVariable Integer courseId,
            @Parameter(description = "Page number (1-indexed)")
            @RequestParam(defaultValue = "1") int page
    ) {
        CourseReviewsDTO reviews = courseDetailService.getCourseReviews(courseId, page);
        if (reviews == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Gets course curriculum/syllabus.
     *
     * @param courseId the course ID
     * @param couponCode optional coupon code
     * @return CurriculumDTO with course sections
     */
    @GetMapping("/{courseId}/curriculum")
    @Operation(summary = "Get course curriculum", 
               description = "Returns the course curriculum/syllabus with all sections and lectures")
    public ResponseEntity<CurriculumDTO> getCourseCurriculum(
            @Parameter(description = "Course ID", required = true)
            @PathVariable Integer courseId,
            @Parameter(description = "Optional coupon code")
            @RequestParam(required = false) String couponCode
    ) {
        CurriculumDTO curriculum = courseDetailService.getCourseCurriculum(courseId, couponCode);
        if (curriculum == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(curriculum);
    }
    
    /**
     * Gets related/recommended courses.
     *
     * @param courseId the course ID
     * @return List of RelatedCourseDTO
     */
    @GetMapping("/{courseId}/related")
    @Operation(summary = "Get related courses", 
               description = "Returns a list of related/recommended courses")
    public ResponseEntity<List<RelatedCourseDTO>> getRelatedCourses(
            @Parameter(description = "Course ID", required = true)
            @PathVariable Integer courseId
    ) {
        List<RelatedCourseDTO> relatedCourses = courseDetailService.getRelatedCourses(courseId);
        return ResponseEntity.ok(relatedCourses);
    }
}

