package com.thanh0x.coursedeal.crawlerrunner.base

/**
 * This class is an abstract base class for coupon url crawlers.
 * It contains a field storing the API URL used for crawling.
 *
 * Child classes must implement the getAllCouponUrls method to retrieve a list of coupon URLs.
 */
abstract class CouponUrlCrawlerBase {
    var apiUrl: String? = null

    /**
     * Returns a list of strings representing all coupon URLs.
     * Subclasses must implement this method to provide the actual implementation.
     *
     * @return a list of strings representing all coupon URLs
     */
    abstract fun getAllCouponUrls(): List<String>
}
