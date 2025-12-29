package com.huythanh0x.udemycoupons.service;

import com.huythanh0x.udemycoupons.crawler_runner.fetcher.WebContentFetcher;
import com.huythanh0x.udemycoupons.utils.UrlUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for making API calls to Udemy's public API endpoints.
 * Handles fetching course details, reviews, curriculum, and related courses.
 * Uses the same WebContentFetcher approach as UdemyCouponCourseExtractor to avoid bot detection.
 */
@Service
public class UdemyApiClient {
    private static final Logger log = LoggerFactory.getLogger(UdemyApiClient.class);
    
    /**
     * Fetches basic course information from Udemy API.
     * Uses the same URL pattern as UdemyCouponCourseExtractor.
     *
     * @param courseId the course ID
     * @return JSONObject containing course details, or null if fetch fails
     */
    public JSONObject getCourseDetailsJson(int courseId) {
        String url = UrlUtils.getCourseAPI(courseId);
        log.debug("Fetching course details for courseId: {}", courseId);
        return WebContentFetcher.getJsonObjectFrom(url);
    }
    
    /**
     * Fetches course landing components including curriculum, pricing, and incentives.
     * Uses the same URL pattern as UdemyCouponCourseExtractor.
     *
     * @param courseId the course ID
     * @param couponCode optional coupon code (can be null)
     * @return JSONObject containing landing components, or null if fetch fails
     */
    public JSONObject getCourseLandingComponentsJson(int courseId, String couponCode) {
        String url = UrlUtils.getCouponAPI(courseId, couponCode);
        log.debug("Fetching landing components for courseId: {}, couponCode: {}", courseId, couponCode);
        return WebContentFetcher.getJsonObjectFrom(url);
    }
    
    /**
     * Fetches course reviews from Udemy API.
     *
     * @param courseId the course ID
     * @param page the page number (default: 1)
     * @return JSONObject containing reviews, or null if fetch fails
     */
    public JSONObject getCourseReviewsJson(int courseId, int page) {
        String url = UrlUtils.getReviewsAPI(courseId, page);
        log.debug("Fetching reviews for courseId: {}, page: {}", courseId, page);
        return WebContentFetcher.getJsonObjectFrom(url);
    }
    
    /**
     * Fetches related/recommended courses from Udemy API.
     *
     * @param courseId the course ID
     * @return JSONObject containing related courses, or null if fetch fails
     */
    public JSONObject getRelatedCoursesJson(int courseId) {
        String url = UrlUtils.getDiscoveryUnitsAPI(courseId);
        log.debug("Fetching related courses for courseId: {}", courseId);
        return WebContentFetcher.getJsonObjectFrom(url);
    }

    /**
     * Fetches detailed information about a specific asset (e.g. course preview video).
     *
     * @param assetId the asset ID
     * @return JSONObject containing asset details, or null if fetch fails
     */
    public JSONObject getAssetJson(long assetId) {
        String url = UrlUtils.getAssetAPI(assetId);
        log.debug("Fetching asset details for assetId: {}", assetId);
        return WebContentFetcher.getJsonObjectFrom(url);
    }

    /**
     * Fetches the course preview page HTML and extracts the embedded JSON data.
     * The preview page contains preview video data in a data-module-args attribute.
     *
     * @param courseId the course ID
     * @param startPreviewId optional preview ID to start with (can be null)
     * @return JSONObject containing preview data with "previews" array, or null if fetch/parse fails
     */
    public JSONObject getPreviewPageJson(int courseId, Long startPreviewId) {
        String url = UrlUtils.getPreviewPageURL(courseId, startPreviewId);
        log.debug("Fetching preview page for courseId: {}, startPreviewId: {}", courseId, startPreviewId);
        
        WebContentFetcher fetcher = new WebContentFetcher();
        Document doc = fetcher.getHtmlDocumentFrom(url);
        if (doc == null) {
            log.warn("Failed to fetch preview page HTML from {}", url);
            return null;
        }
        
        // Find the element with data-module-id="course-preview"
        Element previewElement = doc.selectFirst("[data-module-id=course-preview]");
        if (previewElement == null) {
            log.warn("Could not find course-preview element in preview page HTML");
            return null;
        }
        
        // Extract data-module-args attribute (contains HTML-encoded JSON)
        String moduleArgs = previewElement.attr("data-module-args");
        if (moduleArgs == null || moduleArgs.isEmpty()) {
            log.warn("data-module-args attribute is empty or missing");
            return null;
        }
        
        try {
            // The JSON is HTML-encoded, so we need to decode it
            // Jsoup already decodes HTML entities, but we might need to handle &quot; etc.
            String decodedJson = moduleArgs
                    .replace("&quot;", "\"")
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">");
            
            return new JSONObject(decodedJson);
        } catch (Exception e) {
            log.warn("Failed to parse JSON from data-module-args: {}", e.getMessage());
            return null;
        }
    }
}

