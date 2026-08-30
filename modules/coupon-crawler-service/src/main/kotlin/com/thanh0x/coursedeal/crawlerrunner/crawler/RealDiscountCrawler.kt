package com.thanh0x.coursedeal.crawlerrunner.crawler

import com.thanh0x.coursedeal.config.CrawlerProperties
import com.thanh0x.coursedeal.config.logger
import com.thanh0x.coursedeal.crawlerrunner.base.CouponUrlCrawlerBase
import com.thanh0x.coursedeal.crawlerrunner.fetcher.WebContentFetcher
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.stereotype.Component

/**
 * RealDiscountCrawler class extends CouponUrlCrawlerBase and implements a method to fetch
 * coupon URLs from the Real Discount API.
 */
@Component
class RealDiscountCrawler(
    private val properties: CrawlerProperties,
) : CouponUrlCrawlerBase() {
    private val log = logger()

    init {
        this.apiUrl =
            "https://cdn.real.discount/api/courses?page=1&limit=${properties.numberOfRealDiscountCoupon}" +
            "&sortBy=sale_start"
    }

    /**
     * Retrieves a list of all coupon URLs from the API.
     *
     * @return List<String> containing all coupon URLs
     */
    override fun getAllCouponUrls(): List<String> {
        val jsonArray = fetchListJsonFromAPI(apiUrl!!)
        val allUrls = mutableListOf<String>()
        for (jo in jsonArray) {
            val jsonObject = jo as JSONObject
            allUrls.add(extractCouponUrl(jsonObject))
        }
        log.info(
            "Fetched {} coupons from RealDiscount (requested {})",
            jsonArray.length(),
            properties.numberOfRealDiscountCoupon,
        )
        return allUrls
    }

    /**
     * Extracts the coupon URL from the given JSONObject by removing
     * the specified predefined string from it.
     *
     * @param jsonObject JSONObject containing the coupon URL
     * @return Extracted coupon URL
     */
    fun extractCouponUrl(jsonObject: JSONObject): String {
        return jsonObject.getString("url")
    }

    /**
     * Fetches a JSONArray from a given API URL.
     *
     * @param apiUrl the URL of the API to fetch the JSON data from
     * @return a JSONArray containing the results fetched from the API, or an empty JSONArray if the request failed
     */
    fun fetchListJsonFromAPI(apiUrl: String): JSONArray {
        val jsonObject = WebContentFetcher.getJsonObjectFrom(apiUrl)
        if (jsonObject == null) {
            log.warn("Failed to fetch JSON from {}, returning empty array", apiUrl)
            return JSONArray()
        }
        return try {
            jsonObject.getJSONArray("items")
        } catch (e: JSONException) {
            log.warn("Error extracting 'items' array from JSON: {}", e.message)
            JSONArray()
        }
    }
}
