package com.huythanh0x.udemycoupons.model.coupon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseJsonData {
    private String category;
    private String subCategory;
    private String courseTitle;
    private String level;
    private String author;
    private int contentLength;
    private float rating;
    private int numberReviews;
    private int students;
    private String language;
    private String headline;
    private String description;
}
