package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.crawler;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.base.CouponUrlCrawlerBase;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.fetcher.WebContentFetcher;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * RealDiscountCrawler class extends CouponUrlCrawlerBase and implements a method to fetch coupon URLs from the Real Discount API.
 */
@Component
public class RealDiscountCrawler extends CouponUrlCrawlerBase {
    int maxCouponRequest;
    String apiUrl;

    RealDiscountCrawler(@Value("${custom.number-of-real-discount-coupon}") int maxCouponRequest) {
        this.maxCouponRequest = maxCouponRequest;
        this.apiUrl = String.format("https://cdn.real.discount/api/courses?page=1&limit=%s&sortBy=sale_start", maxCouponRequest);
    }

    /**
     * Retrieves a list of all coupon URLs from the API.
     *
     * @return List<String> containing all coupon URLs
     */
    @Override
    public List<String> getAllCouponUrls() {
        var jsonArray = fetchListJsonFromAPI(apiUrl);
        List<String> allUrls = new ArrayList<>();
        for (var jo : jsonArray) {
            JSONObject jsonObject = (JSONObject) jo;
            allUrls.add(extractCouponUrl(jsonObject));
        }
        System.out.println("Realdiscount jsonArray length: " + jsonArray.length() + " maxCouponRequest: " + maxCouponRequest);
        return allUrls;
    }

    /**
     * Extracts the coupon URL from the given JSONObject by removing
     * the specified predefined string from it.
     *
     * @param jsonObject JSONObject containing the coupon URL
     * @return Extracted coupon URL
     */
    String extractCouponUrl(JSONObject jsonObject) {
        return jsonObject.getString("url");
    }

    /**
     * Fetches a JSONArray from a given API URL.
     *
     * @param apiUrl the URL of the API to fetch the JSON data from
     * @return a JSONArray containing the results fetched from the API
     */
    public JSONArray fetchListJsonFromAPI(String apiUrl) {
        return new JSONArray(WebContentFetcher.getJsonObjectFrom(apiUrl).getJSONArray("items"));
    }
}
