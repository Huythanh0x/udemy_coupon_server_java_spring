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

/**
 * A class that extracts Udemy coupon course data from the provided coupon URL.
 */
public class UdemyCouponCourseExtractor {
    private final String couponUrl;
    private int courseId = 0;
    private String couponCode = "";

    public UdemyCouponCourseExtractor(String couponUrl) {
        this.couponUrl = couponUrl;
        courseId = extractCourseId();
        couponCode = extractCouponCode();
    }

    /**
     * Extracts the course ID from the HTML document retrieved from a given coupon URL.
     * If the data-clp-course-id attribute is found in the body of the document, it is parsed
     * and returned as an integer. If not found, it checks for the element with the id "udemy"
     * and retrieves the data-clp-course-id attribute from it. If neither is found, returns -1.
     * @return The course ID as an integer if found, -1 otherwise.
     */
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

    /**
     * Extracts the coupon code from a given coupon URL by splitting the URL at "/?couponCode="
     * and returning the second element of the resulting array.
     *
     * @return the extracted coupon code
     */
    private String extractCouponCode() {
        return couponUrl.split("/?couponCode=")[1];
    }

    /**
     * Retrieves full coupon code data for a specific course.
     * Makes API calls to extract coupon data and course data from official APIs.
     * Combines the extracted data to create a CouponCourseData object.
     *
     * @return CouponCourseData object containing both coupon data and course data
     */
    public CouponCourseData getFullCouponCodeData() {
        CouponJsonData couponDataResult = extractDataCouponFromOfficialAPI(
                WebContentFetcher.getJsonObjectFrom(UrlUtils.getCouponAPI(courseId, couponCode))
        );
        CourseJsonData courseDataResult = extractCourseDataFromOfficialAPI(
                WebContentFetcher.getJsonObjectFrom(UrlUtils.getCourseAPI(courseId))
        );
        return combineCourseAndCouponData(couponDataResult, courseDataResult);
    }

    /**
     * Extracts course data from the provided JSONObject representing a course object in an official API response.
     *
     * @param courseObjectJson the JSONObject containing course data
     * @return CourseJsonData object with extracted course information
     */
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

    /**
     * Extracts data from the given coupon JSON object retrieved from the official API.
     *
     * @param couponJsonObject JSON object containing coupon data
     * @return CouponJsonData object with extracted data
     */
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

    /**
     * Combines course and coupon data to create a new CouponCourseData object.
     * If the coupon price is not 0, null is returned.
     *
     * @param couponData The CouponJsonData object containing coupon information
     * @param courseData The CourseJsonData object containing course information
     * @return A new CouponCourseData object with combined data from course and coupon
     */
    private CouponCourseData combineCourseAndCouponData(CouponJsonData couponData, CourseJsonData courseData) {
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
