package com.thanh0x.coursedeal.service

import com.thanh0x.coursedeal.config.logger
import com.thanh0x.coursedeal.dto.RelatedCourseDTO
import org.json.JSONException
import org.json.JSONObject
import org.springframework.stereotype.Component

/**
 * Parses related/recommended course data from the Udemy discovery-units API response.
 */
@Component
class RelatedCourseParser {
    private val log = logger()

    fun parseAll(relatedCoursesResponse: JSONObject): List<RelatedCourseDTO> =
        try {
            val items =
                relatedCoursesResponse.optJSONArray("units")
                    ?.optJSONObject(0)
                    ?.optJSONArray("items")

            items?.let { arr ->
                (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.let(::parseOne) }
            } ?: emptyList()
        } catch (e: JSONException) {
            log.error("Error parsing related courses", e)
            emptyList()
        }

    private fun parseOne(courseObj: JSONObject): RelatedCourseDTO? =
        try {
            val author =
                courseObj.optJSONArray("visible_instructors")
                    ?.optJSONObject(0)
                    ?.optString("display_name", "Unknown")
                    ?: "Unknown"

            RelatedCourseDTO(
                courseId = courseObj.optInt("id", 0),
                title = courseObj.optString("title", ""),
                headline = courseObj.optString("headline", ""),
                url = "https://www.udemy.com" + courseObj.optString("url", ""),
                image240x135 = courseObj.optString("image_240x135", ""),
                image480x270 = courseObj.optString("image_480x270", ""),
                image750x422 = courseObj.optString("image_750x422", ""),
                author = author,
                rating = courseObj.optDouble("rating", 0.0).toFloat(),
                numReviews = courseObj.optInt("num_reviews", 0),
                numSubscribers = courseObj.optInt("num_subscribers", 0),
                contentInfo = courseObj.optString("content_info_short", ""),
                instructionalLevel = courseObj.optString("instructional_level_simple", ""),
            )
        } catch (e: JSONException) {
            log.error("Error parsing related course", e)
            null
        }
}
