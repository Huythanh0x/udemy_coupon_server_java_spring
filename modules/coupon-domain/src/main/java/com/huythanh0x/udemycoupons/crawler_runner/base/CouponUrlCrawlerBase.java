package com.huythanh0x.udemycoupons.crawler_runner.base;

import java.util.List;

/**
 * This class is an abstract base class for coupon url crawlers.
 * It contains a field storing the API URL used for crawling.
 * <p>
 * Child classes must implement the getAllCouponUrls method to retrieve a list of coupon URLs.
 */
public abstract class CouponUrlCrawlerBase {
    String apiUrl;

    /**
     * Returns a list of strings representing all coupon URLs.
     * Subclasses must implement this method to provide the actual implementation.
     *
     * @return a list of strings representing all coupon URLs
     */
    public abstract List<String> getAllCouponUrls();
}
