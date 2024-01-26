package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.fetcher.WebContentFetcher;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.coupon.CouponCourseData;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.coupon.CouponJsonData;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.coupon.CourseJsonData;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.utils.UrlUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class UdemyCouponCourseExtractor {
    private final String couponUrl;
    private int courseId = 0;
    private String couponCode = "";

    public UdemyCouponCourseExtractor(String couponUrl) {
        this.couponUrl = couponUrl;
        courseId = extractCourseId();
        couponCode = extractCouponCode();
    }

    private int extractCourseId() {
        Document document = new WebContentFetcher().getHtmlDocumentFrom(couponUrl);
        try {
            return Integer.parseInt(document.body().attr("data-clp-course-id"));
        } catch (Exception e) {
            Element udemyId = document.getElementById("udemy");
            if (udemyId != null) {
                return Integer.parseInt(udemyId.attr("data-clp-course-id"));
            } else {
                return -1;
            }
        }
    }

    private String extractCouponCode() {
        return couponUrl.split("/?couponCode=")[1];
    }

    public CouponCourseData getFullCouponCodeData() {
        CouponJsonData couponDataResult = extractDataCouponFromOfficialAPI(
                WebContentFetcher.getJsonObjectFrom(UrlUtils.getCouponAPI(courseId, couponCode))
        );
        CourseJsonData courseDataResult = extractCourseDataFromOfficialAPI(
                WebContentFetcher.getJsonObjectFrom(UrlUtils.getCourseAPI(courseId))
        );
        return combineCourseAndCouponData(couponDataResult, courseDataResult);
    }

    private CourseJsonData extractCourseDataFromOfficialAPI(JSONObject courseObjectJson) {
        String author = "Unknown";
        String category = "Unknown";
        String subCategory = "Unknown";

        String title = courseObjectJson.optString("title", "");
        String headline = courseObjectJson.optString("headline", "");
        String description = courseObjectJson.optString("description", "").trim().replace("\n", "");
        JSONArray visibleInstructors = courseObjectJson.optJSONArray("visible_instructors");
        if (visibleInstructors != null && !visibleInstructors.isEmpty()) {
            JSONObject instructor = visibleInstructors.optJSONObject(0);
            if (instructor != null) {
                author = instructor.optString("title", "Unknown");
            }
        }
        JSONObject primaryCategory = courseObjectJson.optJSONObject("primary_category");
        if (primaryCategory != null) {
            category = primaryCategory.optString("title", "Unknown");
        }
        JSONObject primarySubCategory = courseObjectJson.optJSONObject("primary_sub_category");
        if (primarySubCategory != null) {
            subCategory = primarySubCategory.optString("title", "Unknown");
        }
        String language = courseObjectJson.optJSONObject("locale").optString("simple_english_title", "");
        String instructionalLevel = courseObjectJson.optString("instructional_level", "");
        String level = instructionalLevel.contains("Levels") ? instructionalLevel : instructionalLevel.replace(" Level", "");
        int students = courseObjectJson.optInt("num_subscribers", 0);
        float rating = courseObjectJson.optFloat("avg_rating_recent", 0.0f);
        int numberReviews = courseObjectJson.optInt("num_reviews", 0);
        int contentLength = courseObjectJson.optInt("estimated_content_length", 0);

        return new CourseJsonData(
                category, subCategory, title, level, author, contentLength, rating, numberReviews, students,
                language, headline, description
        );
    }

    private CouponJsonData extractDataCouponFromOfficialAPI(JSONObject couponJsonObject) {
        int usesRemaining = 0;

        float price = couponJsonObject.optJSONObject("price_text").optJSONObject("data")
                .optJSONObject("pricing_result").optJSONObject("price").optFloat("amount");

        String expiredDate;
        try {
            expiredDate = couponJsonObject.optJSONObject("price_text").optJSONObject("data")
                    .optJSONObject("pricing_result").optJSONObject("campaign").optString("end_time");
        } catch (Exception e) {
            expiredDate = "2030-05-19 17:24:00+00:00";
        }

        String previewImage = couponJsonObject.optJSONObject("sidebar_container").optJSONObject("componentProps")
                .optJSONObject("introductionAsset").optJSONObject("images").optString("image_750x422");

        String previewVideo = couponJsonObject.optJSONObject("sidebar_container").optJSONObject("componentProps")
                .optJSONObject("introductionAsset").optString("course_preview_path");

        try {
            usesRemaining = couponJsonObject.optJSONObject("price_text").optJSONObject("data")
                    .optJSONObject("pricing_result").optJSONObject("campaign").optInt("uses_remaining");
        } catch (Exception ignored) {
        }

        return new CouponJsonData(price, expiredDate, previewImage, previewVideo, usesRemaining);
    }

    private CouponCourseData combineCourseAndCouponData(CouponJsonData couponData, CourseJsonData courseData){
        if (couponData.getPrice() != 0f) return null;
        return new CouponCourseData(
                courseId,
                courseData.getCategory(),
                courseData.getSubCategory(),
                courseData.getCourseTitle(),
                courseData.getContentLength(),
                courseData.getLevel(),
                courseData.getAuthor(),
                courseData.getRating(),
                courseData.getNumberReviews(),
                courseData.getStudents(),
                couponCode,
                couponData.getPreviewImage(),
                couponUrl,
                couponData.getExpiredDate(),
                couponData.getUsesRemaining(),
                courseData.getHeadline(),
                courseData.getDescription(),
                couponData.getPreviewVideo(),
                courseData.getLanguage()
        );
    }
}
