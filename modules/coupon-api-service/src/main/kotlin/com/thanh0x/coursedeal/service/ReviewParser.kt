package com.thanh0x.coursedeal.service

import com.thanh0x.coursedeal.config.logger
import com.thanh0x.coursedeal.dto.CourseReviewsDTO
import com.thanh0x.coursedeal.dto.ReviewDTO
import com.thanh0x.coursedeal.dto.ReviewResponseDTO
import com.thanh0x.coursedeal.dto.ReviewUserDTO
import com.thanh0x.coursedeal.dto.ReviewsSummaryDTO
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.stereotype.Component

/**
 * Parses course review data from the Udemy reviews API response.
 */
@Component
class ReviewParser {
    private val log = logger()

    companion object {
        private const val MAX_RECENT_REVIEWS = 5
    }

    fun parseSummary(reviewsResponse: JSONObject): ReviewsSummaryDTO? =
        try {
            ReviewsSummaryDTO(
                totalCount = reviewsResponse.optInt("count", 0),
                // Calculate from reviews if needed
                averageRating = null,
                recentReviews = parseRecentReviews(reviewsResponse.optJSONArray("results")),
            )
        } catch (e: JSONException) {
            log.error("Error parsing reviews summary", e)
            null
        }

    private fun parseRecentReviews(results: JSONArray?): List<ReviewDTO> =
        results?.let { arr ->
            (0 until minOf(MAX_RECENT_REVIEWS, arr.length())).mapNotNull { i ->
                arr.optJSONObject(i)?.let(::parseReview)
            }
        } ?: emptyList()

    fun parsePaginated(
        reviewsResponse: JSONObject,
        page: Int,
    ): CourseReviewsDTO? =
        try {
            reviewsResponse.optJSONArray("results")?.let { arr ->
                CourseReviewsDTO(
                    reviews = (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.let(::parseReview) },
                    totalCount = reviewsResponse.optInt("count", 0),
                    currentPage = page,
                    hasNext = reviewsResponse.optString("next", null) != null,
                    hasPrevious = reviewsResponse.optString("previous", null) != null,
                    nextUrl = reviewsResponse.optString("next", null),
                    previousUrl = reviewsResponse.optString("previous", null),
                )
            }
        } catch (e: JSONException) {
            log.error("Error parsing course reviews", e)
            null
        }

    fun parseReview(reviewObj: JSONObject): ReviewDTO =
        ReviewDTO(
            id = reviewObj.optLong("id", 0),
            content = reviewObj.optString("content", ""),
            contentHtml = reviewObj.optString("content_html", ""),
            rating = reviewObj.optDouble("rating", 0.0).toFloat(),
            created = reviewObj.optString("created", ""),
            createdFormatted = reviewObj.optString("created_formatted_with_time_since", ""),
            user = reviewObj.optJSONObject("user")?.let(::parseReviewUser),
            response = reviewObj.optJSONObject("response")?.let(::parseResponse),
        )

    private fun parseReviewUser(userObj: JSONObject): ReviewUserDTO =
        ReviewUserDTO(
            displayName = userObj.optString("display_name", ""),
            publicDisplayName = userObj.optString("public_display_name", ""),
            image50x50 = userObj.optString("image_50x50", ""),
            initials = userObj.optString("initials", ""),
        )

    private fun parseResponse(responseObj: JSONObject): ReviewResponseDTO =
        ReviewResponseDTO(
            content = responseObj.optString("content", ""),
            contentHtml = responseObj.optString("content_html", ""),
            created = responseObj.optString("created", ""),
            createdFormatted = responseObj.optString("created_formatted_with_time_since", ""),
            user = responseObj.optJSONObject("user")?.let(::parseReviewUser),
        )
}
