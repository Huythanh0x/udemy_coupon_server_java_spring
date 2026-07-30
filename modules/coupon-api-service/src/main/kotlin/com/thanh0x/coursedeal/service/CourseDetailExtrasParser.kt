package com.thanh0x.coursedeal.service

import com.thanh0x.coursedeal.config.logger
import com.thanh0x.coursedeal.dto.IncentivesDTO
import com.thanh0x.coursedeal.dto.PreviewVideoDTO
import com.thanh0x.coursedeal.dto.PricingInfoDTO
import com.thanh0x.coursedeal.dto.VideoSourceDTO
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.stereotype.Component

/**
 * Parses pricing, incentives, and preview-video data from the Udemy landing-components
 * and preview-page API responses.
 */
@Component
class CourseDetailExtrasParser {
    private val log = logger()

    fun parsePricingInfo(landingComponents: JSONObject): PricingInfoDTO? =
        try {
            val pricingResult =
                landingComponents.optJSONObject("price_text")
                    ?.optJSONObject("data")
                    ?.optJSONObject("pricing_result")

            pricingResult?.let {
                val price = it.optJSONObject("price")
                PricingInfoDTO(
                    price = price?.optDouble("amount", 0.0)?.toFloat(),
                    listPrice = it.optJSONObject("list_price")?.optDouble("amount", 0.0)?.toFloat(),
                    savingPrice = it.optJSONObject("saving_price")?.optDouble("amount", 0.0)?.toFloat(),
                    currency = price?.optString("currency", "") ?: "",
                    priceString = price?.optString("price_string", "") ?: "",
                    currencySymbol = price?.optString("currency_symbol", "") ?: "",
                    discountPercent = it.optInt("discount_percent_for_display", 0),
                    discountDeadlineText = resolveDiscountDeadlineText(landingComponents),
                    couponCode = it.optString("code", ""),
                    usesRemaining = it.optJSONObject("campaign")?.optInt("uses_remaining", 0),
                    maximumUses = it.optJSONObject("campaign")?.optInt("maximum_uses", 0),
                )
            }
        } catch (e: JSONException) {
            log.error("Error parsing pricing info", e)
            null
        }

    private fun resolveDiscountDeadlineText(landingComponents: JSONObject): String? =
        landingComponents.optJSONObject("discount_expiration")
            ?.optJSONObject("data")
            ?.optString("discount_deadline_text", null)

    fun parseIncentives(landingComponents: JSONObject): IncentivesDTO? =
        try {
            landingComponents.optJSONObject("incentives")?.let {
                IncentivesDTO(
                    videoContentLength = it.optString("video_content_length", ""),
                    numArticles = it.optInt("num_articles", 0),
                    numQuizzes = it.optInt("num_quizzes", 0),
                    numPracticeTests = it.optInt("num_practice_tests", 0),
                    numCodingExercises = it.optInt("num_coding_exercises", 0),
                    hasLifetimeAccess = it.optBoolean("has_lifetime_access", false),
                    devicesAccess = it.optString("devices_access", ""),
                    hasAssignments = it.optBoolean("has_assignments", false),
                    hasCertificate = it.optBoolean("has_certificate", false),
                    hasClosedCaptions = it.optBoolean("has_closed_captions", false),
                )
            }
        } catch (e: JSONException) {
            log.error("Error parsing incentives", e)
            null
        }

    fun parsePreviewVideos(previewPageJson: JSONObject): List<PreviewVideoDTO> =
        try {
            previewPageJson.optJSONArray("previews")?.let { arr ->
                (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.let(::parsePreviewVideo) }
            } ?: emptyList()
        } catch (e: JSONException) {
            log.error("Error parsing preview videos", e)
            emptyList()
        }

    private fun parsePreviewVideo(previewObj: JSONObject): PreviewVideoDTO? =
        try {
            PreviewVideoDTO(
                id = previewObj.optLong("id", 0),
                title = previewObj.optString("title", ""),
                thumbnailUrl = previewObj.optString("thumbnail_url", ""),
                contentSummary = previewObj.optString("content_summary", ""),
                timeEstimation = previewObj.optInt("time_estimation", 0),
                // HLS m3u8 URL from media_sources
                videoUrl = previewObj.optJSONArray("media_sources")?.optJSONObject(0)?.optString("src", ""),
                // MP4 files at different resolutions
                streamUrls = extractStreamUrls(previewObj).ifEmpty { null },
            )
        } catch (e: JSONException) {
            log.error("Error parsing preview video", e)
            null
        }

    private fun extractStreamUrls(previewObj: JSONObject): List<VideoSourceDTO> =
        previewObj.optJSONObject("stream_urls")
            ?.optJSONArray("Video")
            ?.let(::parseStreamSources)
            ?: emptyList()

    private fun parseStreamSources(videoStreams: JSONArray): List<VideoSourceDTO> =
        (0 until videoStreams.length()).mapNotNull { i ->
            videoStreams.optJSONObject(i)?.let { streamObj ->
                VideoSourceDTO(
                    type = streamObj.optString("type", ""),
                    label = streamObj.optString("label", ""),
                    file = streamObj.optString("file", ""),
                ).takeIf { it.file?.isNotEmpty() == true }
            }
        }
}
