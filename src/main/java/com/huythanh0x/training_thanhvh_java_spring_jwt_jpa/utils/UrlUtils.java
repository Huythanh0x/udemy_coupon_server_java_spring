package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class UrlUtils {
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

    public static String getCourseAPI(int courseId) {
        return "https://www.udemy.com/api-2.0/courses/" + courseId
                + "/?fields[course]=title,context_info,primary_category,primary_subcategory,"
                + "avg_rating_recent,visible_instructors,locale,estimated_content_length,"
                + "num_subscribers,num_reviews,description,headline,instructional_level";
    }

    public static String decodeBase64String(String encodedBase64String) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedBase64String);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
