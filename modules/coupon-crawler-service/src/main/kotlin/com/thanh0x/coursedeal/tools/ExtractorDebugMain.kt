package com.thanh0x.coursedeal.tools

import com.thanh0x.coursedeal.crawler_runner.CourseDataExtractor

/**
 * Debug entrypoint to run [CourseDataExtractor] for a single URL.
 *
 * Usage (example):
 * UDEMY_DEBUG_DUMP=true UDEMY_DEBUG_URL="<couponUrl>" \
 *   ./gradlew :modules:coupon-crawler-service:debugExtractor
 */
object ExtractorDebugMain {
    @JvmStatic
    fun main(args: Array<String>) {
        var url = extractUrlArg(args)
        if (url.isNullOrBlank()) {
            url = System.getenv("UDEMY_DEBUG_URL")
        }
        if (url.isNullOrBlank()) {
            url = System.getProperty("udemy.debugUrl")
        }
        if (url.isNullOrBlank()) {
            throw IllegalArgumentException("Missing URL. Provide --url=<couponUrl> or set UDEMY_DEBUG_URL.")
        }

        val extractor = CourseDataExtractor(url.trim())
        val result = extractor.getFullCouponCodeData()

        println("==== Extractor Debug Result ====")
        println("inputUrl=$url")
        println("courseId=${extractor.courseId}")
        println("result=${result ?: "null"}")
    }

    private fun extractUrlArg(args: Array<String>?): String? {
        if (args == null) return null
        for (arg in args) {
            if (arg.startsWith("--url=")) return arg.substring("--url=".length)
            if (arg == "--url") continue // next arg handled in a second pass below
        }

        // fallback: look for pattern "--url" <value>
        for (i in 0 until args.size - 1) {
            if ("--url" == args[i]) return args[i + 1]
        }
        return null
    }
}
