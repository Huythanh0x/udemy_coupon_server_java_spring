package com.thanh0x.coursedeal.crawler_runner

import com.thanh0x.coursedeal.config.logger
import com.thanh0x.coursedeal.crawler_runner.fetcher.WebContentFetcher
import com.thanh0x.coursedeal.model.coupon.CouponCourseData
import com.thanh0x.coursedeal.model.coupon.CouponJsonData
import com.thanh0x.coursedeal.model.coupon.CourseJsonData
import com.thanh0x.coursedeal.model.coupon.CourseLevel
import com.thanh0x.coursedeal.utils.UrlUtils
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * A class that extracts course coupon data from the provided coupon URL.
 */
class CourseDataExtractor {
    private val log = logger()
    private val couponUrl: String
    var courseId: Int = 0
        private set
    private var couponCode: String = ""

    /**
     * Creates a new CourseDataExtractor with the given coupon URL.
     * Will attempt to extract courseId from the URL's HTML page.
     *
     * @param couponUrl The coupon URL to extract data from
     */
    constructor(couponUrl: String) {
        this.couponUrl = couponUrl
        this.courseId = extractCourseId()
        this.couponCode = extractCouponCode()
    }

    /**
     * Creates a new CourseDataExtractor with the given coupon URL and courseId.
     * Skips the expensive HTTP request to extract courseId from HTML.
     *
     * @param couponUrl The coupon URL to extract data from
     * @param courseId The course ID (if already known from database)
     */
    constructor(couponUrl: String, courseId: Int) {
        this.couponUrl = couponUrl
        this.courseId = courseId
        this.couponCode = extractCouponCode()
    }

    /**
     * Extracts the course ID from the HTML document retrieved from a given coupon URL.
     */
    private fun extractCourseId(): Int {
        val document = WebContentFetcher().getHtmlDocumentFrom(couponUrl)
        if (document == null || document.body() == null) {
            log.warn("Unable to load document for coupon URL {}", couponUrl)
            return -1
        }

        // Try extracting from body data attribute (modern Udemy pages)
        val bodyId = document.body().attr("data-clp-course-id")
        if (bodyId.isNotEmpty()) {
            try {
                return bodyId.toInt()
            } catch (ignored: NumberFormatException) {
            }
        }

        // Fallback to searching the whole HTML for the native deeplink token
        val html = document.html()
        val idFromDiscover = extractCourseIdFromDiscoverDeeplink(html)
        if (idFromDiscover > 0) return idFromDiscover

        log.warn("Course id not found in HTML for {}", couponUrl)
        return -1
    }

    private fun extractCourseIdFromDiscoverDeeplink(html: String?): Int {
        if (html.isNullOrEmpty()) return -1

        val token = "udemy://discover?courseId="
        val idx = html.indexOf(token)
        if (idx < 0) return -1

        val start = idx + token.length
        if (start >= html.length) return -1

        var end = start
        while (end < html.length && html[end].isDigit()) {
            end++
        }

        if (end <= start) return -1

        return try {
            html.substring(start, end).toInt()
        } catch (ignored: Exception) {
            -1
        }
    }

    /**
     * Extracts the coupon code from a given coupon URL by splitting the URL at "/?couponCode="
     * and returning the second element of the resulting array.
     *
     * @return the extracted coupon code
     */
    private fun extractCouponCode(): String {
        return try {
            val parts = couponUrl.split("/?couponCode=")
            if (parts.size > 1) {
                parts[1]
            } else {
                ""
            }
        } catch (e: Exception) {
            log.warn("Failed to extract coupon code from URL {}: {}", couponUrl, e.message)
            ""
        }
    }

    /**
     * Retrieves full coupon code data for a specific course.
     * Makes API calls to extract coupon data and course data from official APIs.
     * Combines the extracted data to create a CouponCourseData object.
     *
     * @return CouponCourseData object containing both coupon data and course data
     */
    fun getFullCouponCodeData(): CouponCourseData? {
        val couponJson = WebContentFetcher.getJsonObjectFrom(UrlUtils.getCouponAPI(courseId, couponCode))
        val courseJson = WebContentFetcher.getJsonObjectFrom(UrlUtils.getCourseAPI(courseId))

        val couponDataResult = extractDataCouponFromOfficialAPI(couponJson)
        val courseDataResult = extractCourseDataFromOfficialAPI(courseJson)
        return combineCourseAndCouponData(couponDataResult, courseDataResult)
    }

    /**
     * Extracts course data from the provided JSONObject representing a course object in an official API response.
     *
     * @param courseObjectJson the JSONObject containing course data
     * @return CourseJsonData object with extracted course information
     */
    private fun extractCourseDataFromOfficialAPI(courseObjectJson: JSONObject?): CourseJsonData? {
        if (courseObjectJson == null) return null

        var author = "Unknown"
        var category = "Unknown"
        var subCategory = "Unknown"

        val title = courseObjectJson.optString("title", "")
        val headline = courseObjectJson.optString("headline", "")
        val description = courseObjectJson.optString("description", "").trim().replace("\n", "")
        val visibleInstructors = courseObjectJson.optJSONArray("visible_instructors")
        if (visibleInstructors != null && !visibleInstructors.isEmpty) {
            val instructor = visibleInstructors.optJSONObject(0)
            if (instructor != null) {
                author = instructor.optString("title", "Unknown")
            }
        }
        val primaryCategory = courseObjectJson.optJSONObject("primary_category")
        if (primaryCategory != null) {
            category = primaryCategory.optString("title", "Unknown")
        }
        val primarySubCategory = courseObjectJson.optJSONObject("primary_sub_category")
        if (primarySubCategory != null) {
            subCategory = primarySubCategory.optString("title", "Unknown")
        }
        val localeObj = courseObjectJson.optJSONObject("locale")
        val language = localeObj?.optString("simple_english_title", "") ?: ""
        val instructionalLevel = courseObjectJson.optString("instructional_level", "")
        val level = if (instructionalLevel.contains("Levels")) instructionalLevel else instructionalLevel.replace(" Level", "")
        val students = courseObjectJson.optInt("num_subscribers", 0)
        val rating = courseObjectJson.optFloat("avg_rating_recent", 0.0f)
        val numberReviews = courseObjectJson.optInt("num_reviews", 0)
        val contentLength = courseObjectJson.optInt("estimated_content_length", 0)

        return CourseJsonData(
            category, subCategory, title, level, author, contentLength, rating, numberReviews, students,
            language, headline, description
        )
    }

    /**
     * Extracts data from the given coupon JSON object retrieved from the official API.
     *
     * @param couponJsonObject JSON object containing coupon data
     * @return CouponJsonData object with extracted data
     */
    private fun extractDataCouponFromOfficialAPI(couponJsonObject: JSONObject?): CouponJsonData? {
        if (couponJsonObject == null) return null

        // Udemy now sometimes returns payloads like: {"detail":"Not found"}
        val detail = couponJsonObject.optString("detail", null)
        if (detail != null) {
            return null
        }

        val priceTextObj = couponJsonObject.optJSONObject("price_text") ?: return null
        val dataObj = priceTextObj.optJSONObject("data") ?: return null
        val pricingResultObj = dataObj.optJSONObject("pricing_result") ?: return null
        val priceObj = pricingResultObj.optJSONObject("price") ?: return null

        val price = priceObj.optFloat("amount", Float.NaN)
        if (java.lang.Float.isNaN(price)) return null

        val campaignObj = pricingResultObj.optJSONObject("campaign")
        val expiredDateStr = campaignObj?.optString("end_time", null)
        var expiredDate: Instant
        try {
            expiredDate = parseExpiredDate(expiredDateStr)
        } catch (e: Exception) {
            log.warn("Failed to parse expired date for {}: {}", couponUrl, e.message)
            expiredDate = Instant.parse("2030-05-19T17:24:00Z")
        }

        var previewImage = ""
        var previewVideo = ""
        val sidebarContainerObj = couponJsonObject.optJSONObject("sidebar_container")
        if (sidebarContainerObj != null) {
            val componentPropsObj = sidebarContainerObj.optJSONObject("componentProps")
            if (componentPropsObj != null) {
                val introductionAssetObj = componentPropsObj.optJSONObject("introductionAsset")
                if (introductionAssetObj != null) {
                    val imagesObj = introductionAssetObj.optJSONObject("images")
                    if (imagesObj != null) {
                        previewImage = imagesObj.optString("image_750x422", "")
                    }
                    previewVideo = introductionAssetObj.optString("course_preview_path", "")
                }
            }
        }

        val usesRemaining = campaignObj?.optInt("uses_remaining", 0) ?: 0
        return CouponJsonData(price, expiredDate, previewImage, previewVideo, usesRemaining)
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
    private fun parseExpiredDate(dateStr: String?): Instant {
        if (dateStr == null || dateStr.trim { it <= ' ' }.isEmpty()) {
            return Instant.parse("2030-05-19T17:24:00Z") // Default far future
        }

        return try {
            if (dateStr.contains("T")) {
                return Instant.parse(dateStr)
            }

            // Handle Udemy format: "2030-05-19 17:24:00+00:00" (space instead of T)
            var normalized = dateStr.trim { it <= ' ' }
            if (normalized.contains(" ")) {
                normalized = normalized.replaceFirst(" ".toRegex(), "T")
                var hasTimezone = normalized.contains("+") || normalized.contains("Z")
                // Also check for negative timezone (e.g., "-05:00") after position 19
                if (!hasTimezone && normalized.length > 19) {
                    val timezoneStart = normalized.indexOf("-", 19)
                    hasTimezone = (timezoneStart > 0 && normalized.length > timezoneStart + 5)
                }
                if (!hasTimezone) {
                    normalized += "Z" // Assume UTC if no timezone
                }
                return Instant.parse(normalized)
            }

            OffsetDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()

        } catch (e: DateTimeParseException) {
            log.debug("Failed to parse expired date string '{}', defaulting far future", dateStr, e)
            Instant.parse("2030-05-19T17:24:00Z")
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
    private fun combineCourseAndCouponData(couponData: CouponJsonData?, courseData: CourseJsonData?): CouponCourseData? {
        if (couponData == null || courseData == null) return null
        if (couponData.price != 0f) return null
        return CouponCourseData(
            courseId = courseId,
            category = courseData.category,
            subCategory = courseData.subCategory,
            title = courseData.courseTitle,
            contentLength = courseData.contentLength,
            level = CourseLevel.fromString(courseData.level),
            author = courseData.author,
            rating = courseData.rating,
            reviews = courseData.numberReviews,
            students = courseData.students,
            couponCode = couponCode,
            previewImage = couponData.previewImage,
            couponUrl = couponUrl,
            expiredDate = couponData.expiredDate,
            usesRemaining = couponData.usesRemaining,
            heading = courseData.headline,
            description = courseData.description,
            previewVideo = couponData.previewVideo,
            language = courseData.language
        )
    }
}
