// java
package com.huythanh0x.udemycoupons.crawler_runner.crawler;

import com.huythanh0x.udemycoupons.crawler_runner.base.CouponUrlCrawlerBase;
import com.huythanh0x.udemycoupons.crawler_runner.fetcher.WebContentFetcher;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Component class for crawling coupon URLs from the Enext site which now renders server-side HTML.
 * Extends CouponUrlCrawlerBase.
 */
@Component
public class EnextCrawler extends CouponUrlCrawlerBase {
    private static final String LIST_PAGE_FORMAT = "https://jobs.e-next.in/course/udemy/%d";
    private static final String SITE_BASE = "https://jobs.e-next.in";

    int maxCouponRequest;

    EnextCrawler(@Value("${custom.number-of-enext-coupon}") int maxCouponRequest) {
        this.maxCouponRequest = maxCouponRequest;
    }

    /**
     * Retrieves all coupon URLs by scraping list pages and then visiting each course detail page
     * to extract the real Udemy coupon link.
     *
     * @return a list of Udemy coupon URLs
     */
    @Override
    public List<String> getAllCouponUrls() {
        List<String> allUrls = new ArrayList<>();
        WebContentFetcher fetcher = new WebContentFetcher();

        int page = 1;
        boolean morePages = true;

        while (morePages && allUrls.size() < maxCouponRequest) {
            String listUrl = String.format(LIST_PAGE_FORMAT, page);
            Document listDoc = fetcher.getHtmlDocumentFrom(listUrl);
            if (listDoc == null) {
                System.out.println("Failed to fetch list page: " + listUrl);
                break;
            }

            // Find course detail links in the list page
            Elements courseAnchors = listDoc.select("div.portfolio-item a[href]");
            if (courseAnchors.isEmpty()) {
                // no items on this page -> stop
                morePages = false;
                break;
            }

            List<String> detailUrlsThisPage = new ArrayList<>();
            for (Element a : courseAnchors) {
                String href = a.attr("href").trim();
                if (href.isEmpty()) continue;
                // Normalize to absolute URL if needed
                String detailUrl = href.startsWith("http") ? href : SITE_BASE + (href.startsWith("/") ? href : ("/" + href));
                detailUrlsThisPage.add(detailUrl);
            }

            // For each detail page, fetch and extract real Udemy coupon link
            for (String detailUrl : detailUrlsThisPage) {
                if (allUrls.size() >= maxCouponRequest) break;
                try {
                    Document detailDoc = fetcher.getHtmlDocumentFrom(detailUrl);
                    if (detailDoc == null) {
                        System.out.println("Failed to fetch detail page: " + detailUrl);
                        continue;
                    }
                    // Coupon button appears as a primary button linking to udemy.com
                    Element couponAnchor = detailDoc.selectFirst("a.btn.btn-primary[href*='udemy.com']");
                    if (couponAnchor == null) {
                        // Some pages might use different classes; try a more lenient selector
                        couponAnchor = detailDoc.selectFirst("a[href*='udemy.com/?couponCode=']");
                    }
                    if (couponAnchor != null) {
                        String udemyUrl = couponAnchor.attr("href").trim();
                        if (!udemyUrl.isEmpty()) {
                            allUrls.add(udemyUrl);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error processing detail page " + detailUrl + " : " + e);
                }
            }

            System.out.println("Enext page " + page + " found " + detailUrlsThisPage.size() + " items, collected so far: " + allUrls.size());

            // proceed to next page
            page++;
        }

        // trim to maxCouponRequest if necessary
        if (allUrls.size() > maxCouponRequest) {
            return allUrls.subList(0, maxCouponRequest);
        }
        return allUrls;
    }
}
