package com.huythanh0x.udemycoupons.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class for handling URLs related to Udemy APIs.
 */
public class UrlUtils {
    /**
     * Returns the API URL for getting information about a course with a specific coupon code.
     *
     * @param courseId the ID of the course to retrieve information for
     * @param couponCode the coupon code to apply to the course
     * @return the API URL for the specified course with the provided coupon code
     */
    public static String getCouponAPI(int courseId, String couponCode) {
        return "https://www.udemy.com/api-2.0/course-landing-components/" + courseId
                + "/me/?couponCode=" + couponCode
                + "&components=deal_badge,discount_expiration,gift_this_course,price_text,purchase,"
                + "recommendation,redeem_coupon,cacheable_deal_badge,cacheable_discount_expiration,"
                + "cacheable_price_text,cacheable_buy_button,buy_button,buy_for_team,"
                + "cacheable_purchase_text,cacheable_add_to_cart,money_back_guarantee,"
                + "instructor_links,incentives_context,top_companies_notice_context,"
                + "curated_for_ufb_notice_context,sidebar_container,purchase_tabs_context,"
                + "subscribe_team_modal_context,lifetime_access_context,available_coupons";
    }

    /**
     * Returns the API endpoint for a specific course on Udemy.
     *
     * @param courseId the unique identifier of the course
     * @return the URL string for the course API including various fields such as title, context info, primary category,
     * primary subcategory, recent average rating, visible instructors, locale, estimated content length, number of subscribers,
     * number of reviews, description, headline, and instructional level
     */
    public static String getCourseAPI(int courseId) {
        return "https://www.udemy.com/api-2.0/courses/" + courseId
                + "/?fields[course]=title,context_info,primary_category,primary_subcategory,"
                + "avg_rating_recent,visible_instructors,locale,estimated_content_length,"
                + "num_subscribers,num_reviews,description,headline,instructional_level";
    }

    /**
     * Returns the API URL for getting course reviews.
     *
     * @param courseId the ID of the course
     * @param page the page number (1-indexed)
     * @return the API URL for course reviews
     */
    public static String getReviewsAPI(int courseId, int page) {
        return "https://www.udemy.com/api-2.0/courses/" + courseId + "/reviews/"
                + "?courseId=" + courseId
                + "&page=" + page
                + "&is_text_review=1"
                + "&ordering=course_review_score__rank,-created"
                + "&fields[course_review]=@default,response,content_html,created_formatted_with_time_since"
                + "&fields[user]=@min,image_50x50,initials,public_display_name,tracking_id"
                + "&fields[course_review_response]=@min,user,content_html,created_formatted_with_time_since";
    }

    /**
     * Returns the API URL for getting related/recommended courses.
     *
     * @param courseId the ID of the course
     * @return the API URL for related courses
     */
    public static String getDiscoveryUnitsAPI(int courseId) {
        return "https://www.udemy.com/api-2.0/discovery-units/"
                + "?context=clp-bundle"
                + "&from=0"
                + "&page_size=3"
                + "&item_count=12"
                + "&course_id=" + courseId
                + "&source_page=course_landing_page"
                + "&locale=en_US"
                + "&currency=vnd"
                + "&navigation_locale=en_US"
                + "&skip_price=true"
                + "&funnel_context=landing-page";
    }

    /**
     * Returns the API URL for getting detailed information about a specific asset
     * (e.g. the course preview video).
     *
     * @param assetId the ID of the asset
     * @return the API URL for the asset with media sources, captions, and thumbnails
     */
    public static String getAssetAPI(long assetId) {
        return "https://www.udemy.com/api-2.0/assets/" + assetId
                + "/?fields[asset]=@min,status,delayed_asset_message,processing_errors,time_estimation,"
                + "media_license_token,media_sources,thumbnail_url,captions,thumbnail_sprite,created"
                + "&fields[caption]=@default,is_translation";
    }

    /**
     * Returns the preview page URL for a course.
     * This page contains embedded JSON with preview video data.
     *
     * @param courseId the ID of the course
     * @param startPreviewId optional preview ID to start with (can be null)
     * @return the preview page URL
     */
    public static String getPreviewPageURL(int courseId, Long startPreviewId) {
        String url = "https://www.udemy.com/course/" + courseId + "/preview/";
        if (startPreviewId != null && startPreviewId > 0) {
            url += "?startPreviewId=" + startPreviewId + "&uiRegion=sidebar.introductionAsset";
        }
        return url;
    }

    /**
     * Decodes a Base64 encoded string into a UTF-8 encoded string.
     *
     * @param encodedBase64String the Base64 encoded string to be decoded
     * @return the decoded UTF-8 string
     */
    public static String decodeBase64String(String encodedBase64String) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedBase64String);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
