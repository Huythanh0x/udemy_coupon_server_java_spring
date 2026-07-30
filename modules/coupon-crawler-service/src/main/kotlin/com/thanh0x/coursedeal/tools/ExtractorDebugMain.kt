package com.thanh0x.coursedeal.tools

import com.thanh0x.coursedeal.crawlerrunner.CourseDataExtractor

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
        val resolvedUrl =
            (extractUrlArg(args) ?: System.getenv("UDEMY_DEBUG_URL") ?: System.getProperty("udemy.debugUrl"))
                ?.trim()
                ?.takeIf { it.isNotBlank() }

        requireNotNull(resolvedUrl) { "Missing URL. Provide --url=<couponUrl> or set UDEMY_DEBUG_URL." }

        val extractor = CourseDataExtractor(resolvedUrl)
        val result = extractor.getFullCouponCodeData()

        println("==== Extractor Debug Result ====")
        println("inputUrl=$resolvedUrl")
        println("courseId=${extractor.courseId}")
        println("result=${result ?: "null"}")
    }

    private fun extractUrlArg(args: Array<String>?): String? {
        if (args == null) return null

        val inlineUrl = args.firstOrNull { it.startsWith("--url=") }?.substringAfter("--url=")
        val separateUrlIndex = args.indexOf("--url")
        val separateUrl = separateUrlIndex.takeIf { it in 0 until args.lastIndex }?.let { args[it + 1] }

        return inlineUrl ?: separateUrl
    }
}
