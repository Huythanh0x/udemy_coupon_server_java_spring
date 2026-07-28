package com.thanh0x.coursedeal.service

import com.thanh0x.coursedeal.crawler_runner.fetcher.WebContentFetcher
import com.thanh0x.coursedeal.utils.UrlUtils
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Service for making API calls to external course public API endpoints (e.g. Udemy).
 * Handles fetching course details, reviews, curriculum, and related courses.
 */
@Service
class ExternalCourseApiClient {

    /**
     * Fetches basic course information from external API.
     */
    fun getCourseDetailsJson(courseId: Int): JSONObject? {
        val url = UrlUtils.getCourseAPI(courseId)
        log.debug("Fetching course details for courseId: {}", courseId)
        return WebContentFetcher.getJsonObjectFrom(url)
    }

    /**
     * Fetches course landing components including curriculum, pricing, and incentives.
     */
    fun getCourseLandingComponentsJson(courseId: Int, couponCode: String?): JSONObject? {
        val url = UrlUtils.getCouponAPI(courseId, couponCode)
        log.debug("Fetching landing components for courseId: {}, couponCode: {}", courseId, couponCode)
        return WebContentFetcher.getJsonObjectFrom(url)
    }

    /**
     * Fetches course reviews from external API.
     */
    fun getCourseReviewsJson(courseId: Int, page: Int): JSONObject? {
        val url = UrlUtils.getReviewsAPI(courseId, page)
        log.debug("Fetching reviews for courseId: {}, page: {}", courseId, page)
        return WebContentFetcher.getJsonObjectFrom(url)
    }

    /**
     * Fetches related/recommended courses from external API.
     */
    fun getRelatedCoursesJson(courseId: Int): JSONObject? {
        val url = UrlUtils.getDiscoveryUnitsAPI(courseId)
        log.debug("Fetching related courses for courseId: {}", courseId)
        return WebContentFetcher.getJsonObjectFrom(url)
    }

    /**
     * Fetches detailed information about a specific asset (e.g. course preview video).
     */
    fun getAssetJson(assetId: Long): JSONObject? {
        val url = UrlUtils.getAssetAPI(assetId)
        log.debug("Fetching asset details for assetId: {}", assetId)
        return WebContentFetcher.getJsonObjectFrom(url)
    }

    /**
     * Fetches the course preview page HTML and extracts the embedded JSON data.
     */
    fun getPreviewPageJson(courseId: Int, startPreviewId: Long?): JSONObject? {
        val url = UrlUtils.getPreviewPageURL(courseId, startPreviewId)
        log.debug("Fetching preview page for courseId: {}, startPreviewId: {}", courseId, startPreviewId)

        val fetcher = WebContentFetcher()
        val doc = fetcher.getHtmlDocumentFrom(url)
        if (doc == null) {
            log.warn("Failed to fetch preview page HTML from {}", url)
            return null
        }

        // Find the element with data-module-id="course-preview"
        val previewElement = doc.selectFirst("[data-module-id=course-preview]")
        if (previewElement == null) {
            log.warn("Could not find course-preview element in preview page HTML")
            return null
        }

        // Extract data-module-args attribute (contains HTML-encoded JSON)
        val moduleArgs = previewElement.attr("data-module-args")
        if (moduleArgs.isEmpty()) {
            log.warn("data-module-args attribute is empty or missing")
            return null
        }

        return try {
            // The JSON is HTML-encoded, so we need to decode it
            val decodedJson = moduleArgs
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")

            JSONObject(decodedJson)
        } catch (e: Exception) {
            log.warn("Failed to parse JSON from data-module-args: {}", e.message)
            null
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ExternalCourseApiClient::class.java)
    }
}
