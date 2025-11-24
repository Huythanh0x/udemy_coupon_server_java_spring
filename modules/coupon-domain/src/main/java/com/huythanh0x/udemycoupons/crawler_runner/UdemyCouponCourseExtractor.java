package com.huythanh0x.udemycoupons.crawler_runner;

import com.huythanh0x.udemycoupons.crawler_runner.fetcher.WebContentFetcher;
import com.huythanh0x.udemycoupons.model.coupon.CouponCourseData;
import com.huythanh0x.udemycoupons.model.coupon.CouponJsonData;
import com.huythanh0x.udemycoupons.model.coupon.CourseJsonData;
import com.huythanh0x.udemycoupons.utils.UrlUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * A class that extracts Udemy coupon course data from the provided coupon URL.
 */
public class UdemyCouponCourseExtractor {
    private final String couponUrl;
    private int courseId = 0;
    private String couponCode = "";

    /**
     * Creates a new UdemyCouponCourseExtractor with the given coupon URL.
     * Will attempt to extract courseId from the URL's HTML page.
     *
     * @param couponUrl The coupon URL to extract data from
     */
    public UdemyCouponCourseExtractor(String couponUrl) {
        this.couponUrl = couponUrl;
        courseId = extractCourseId();
        couponCode = extractCouponCode();
    }

    /**
     * Creates a new UdemyCouponCourseExtractor with the given coupon URL and courseId.
     * Skips the expensive HTTP request to extract courseId from HTML.
     *
     * @param couponUrl The coupon URL to extract data from
     * @param courseId The course ID (if already known from database)
     */
    public UdemyCouponCourseExtractor(String couponUrl, int courseId) {
        this.couponUrl = couponUrl;
        this.courseId = courseId;
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

        Instant expiredDate;
        try {
            String expiredDateStr = couponJsonObject.optJSONObject("price_text").optJSONObject("data")
                    .optJSONObject("pricing_result").optJSONObject("campaign").optString("end_time");
            expiredDate = parseExpiredDate(expiredDateStr);
        } catch (Exception e) {
            expiredDate = Instant.parse("2030-05-19T17:24:00Z");
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
     * Parses an ISO 8601 date string to Instant (UTC).
     * Handles formats like:
     * - "2030-05-19 17:24:00+00:00" (Udemy format with space and timezone)
     * - "2030-05-19T17:24:00Z" (ISO 8601 standard)
     * - "2030-05-19T17:24:00+00:00" (ISO 8601 with timezone)
     *
     * @param dateStr The date string to parse
     * @return Instant representing the date in UTC
     */
    private Instant parseExpiredDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return Instant.parse("2030-05-19T17:24:00Z"); // Default far future
        }

        try {
            if (dateStr.contains("T")) {
                return Instant.parse(dateStr);
            }
            
            // Handle Udemy format: "2030-05-19 17:24:00+00:00" (space instead of T)
            String normalized = dateStr.trim();
            if (normalized.contains(" ")) {
                normalized = normalized.replaceFirst(" ", "T");
                boolean hasTimezone = normalized.contains("+") || normalized.contains("Z");
                // Also check for negative timezone (e.g., "-05:00") after position 19
                if (!hasTimezone && normalized.length() > 19) {
                    int timezoneStart = normalized.indexOf("-", 19);
                    hasTimezone = (timezoneStart > 0 && normalized.length() > timezoneStart + 5);
                }
                if (!hasTimezone) {
                    normalized += "Z"; // Assume UTC if no timezone
                }
                return Instant.parse(normalized);
            }
            
            return OffsetDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
            
        } catch (DateTimeParseException e) {
            return Instant.parse("2030-05-19T17:24:00Z");
        }
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
        return CouponCourseData.builder()
                .courseId(courseId)
                .category(courseData.getCategory())
                .subCategory(courseData.getSubCategory())
                .title(courseData.getCourseTitle())
                .contentLength(courseData.getContentLength())
                .level(courseData.getLevel())
                .author(courseData.getAuthor())
                .rating(courseData.getRating())
                .reviews(courseData.getNumberReviews())
                .students(courseData.getStudents())
                .couponCode(couponCode)
                .previewImage(couponData.getPreviewImage())
                .couponUrl(couponUrl)
                .expiredDate(couponData.getExpiredDate())
                .usesRemaining(couponData.getUsesRemaining())
                .heading(courseData.getHeadline())
                .description(courseData.getDescription())
                .previewVideo(couponData.getPreviewVideo())
                .language(courseData.getLanguage())
                .build();
    }
}
