package com.thanh0x.coursedeal.model.coupon

data class CourseJsonData(
    var category: String? = null,
    var subCategory: String? = null,
    var courseTitle: String? = null,
    var level: String? = null,
    var author: String? = null,
    var contentLength: Int = 0,
    var rating: Float = 0f,
    var numberReviews: Int = 0,
    var students: Int = 0,
    var language: String? = null,
    var headline: String? = null,
    var description: String? = null,
)
