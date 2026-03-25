package com.huythanh0x.udemycoupons.tools;

import com.huythanh0x.udemycoupons.crawler_runner.UdemyCouponCourseExtractor;
import com.huythanh0x.udemycoupons.model.coupon.CouponCourseData;

/**
 * Debug entrypoint to run {@link UdemyCouponCourseExtractor} for a single URL.
 *
 * Usage (example):
 * UDEMY_DEBUG_DUMP=true UDEMY_DEBUG_URL="<couponUrl>" \
 *   ./gradlew :modules:coupon-crawler-service:debugExtractor
 */
public class ExtractorDebugMain {

    public static void main(String[] args) {
        String url = extractUrlArg(args);
        if (url == null || url.isBlank()) {
            url = System.getenv("UDEMY_DEBUG_URL");
        }
        if (url == null || url.isBlank()) {
            url = System.getProperty("udemy.debugUrl");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Missing URL. Provide --url=<couponUrl> or set UDEMY_DEBUG_URL.");
        }

        UdemyCouponCourseExtractor extractor = new UdemyCouponCourseExtractor(url.trim());
        CouponCourseData result = extractor.getFullCouponCodeData();

        System.out.println("==== Extractor Debug Result ====");
        System.out.println("inputUrl=" + url);
        System.out.println("courseId=" + extractor.getCourseId());
        System.out.println("result=" + (result == null ? "null" : result));
    }

    private static String extractUrlArg(String[] args) {
        if (args == null) return null;
        for (String arg : args) {
            if (arg == null) continue;
            if (arg.startsWith("--url=")) return arg.substring("--url=".length());
            if (arg.equals("--url")) continue; // next arg handled in a second pass below
        }

        // fallback: look for pattern "--url" <value>
        for (int i = 0; i < args.length - 1; i++) {
            if ("--url".equals(args[i])) return args[i + 1];
        }
        return null;
    }
}

