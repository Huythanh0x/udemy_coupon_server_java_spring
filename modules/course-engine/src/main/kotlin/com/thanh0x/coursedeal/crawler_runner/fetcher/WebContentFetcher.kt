package com.thanh0x.coursedeal.crawler_runner.fetcher

import com.thanh0x.coursedeal.config.logger
import com.thanh0x.coursedeal.utils.UrlValidator
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * A utility class for fetching and parsing web content.
 */
class WebContentFetcher {
    private val log = logger()

    /**
     * Retrieves a JSONArray object from a specified URL.
     *
     * @param urlString the URL from which to retrieve the JSON array
     * @return a JSONArray object, or null if the request failed
     */
    fun getJsonArrayFrom(urlString: String): JSONArray? {
        if (!UrlValidator.isSafeUrl(urlString)) {
            return null
        }
        val rawHtml = getRawHTMLContentFrom(urlString)
        if (rawHtml.isNullOrBlank()) {
            log.warn("Failed to fetch content from {}, returning null", urlString)
            return null
        }
        return try {
            JSONArray(rawHtml)
        } catch (e: Exception) {
            log.warn("Error parsing JSON array from {}: {}", urlString, e.message)
            null
        }
    }

    /**
     * Retrieves the raw HTML content from a specified URL and parses it into an HTML Document using Jsoup.
     *
     * @param urlString the URL of the webpage to retrieve HTML content from
     * @return an HTML Document, or null if the request failed
     */
    fun getHtmlDocumentFrom(urlString: String): Document? {
        if (!UrlValidator.isSafeUrl(urlString)) {
            return null
        }
        val rawHtml = getRawHTMLContentFrom(urlString)
        if (rawHtml.isNullOrBlank()) {
            log.warn("Failed to fetch content from {}, returning null", urlString)
            return null
        }
        return try {
            Jsoup.parse(rawHtml)
        } catch (e: Exception) {
            log.warn("Error parsing HTML from {}: {}", urlString, e.message)
            null
        }
    }

    companion object {
        private val log = logger()

        /**
         * Retrieves a JSONObject from a given URL.
         *
         * @param urlString the URL from which to retrieve the JSON object
         * @return the JSONObject, or null if the request failed or timed out
         */
        @JvmStatic
        fun getJsonObjectFrom(urlString: String): JSONObject? {
            if (!UrlValidator.isSafeUrl(urlString)) {
                return null
            }
            val content = getRawHTMLContentFrom(urlString)
            if (content.isNullOrBlank()) {
                log.warn("Failed to fetch content from {}, returning null", urlString)
                return null
            }
            return try {
                JSONObject(content)
            } catch (e: Exception) {
                log.warn("Error parsing JSON from {}: {}", urlString, e.message)
                null
            }
        }

        /**
         * Fetches the raw HTML content from a given URL.
         *
         * @param urlString the URL from which to fetch the raw HTML content
         * @return the raw HTML content as a String
         */
        @JvmStatic
        fun getRawHTMLContentFrom(urlString: String): String? {
            val client = HttpClient.newHttpClient()
            val request =
                HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(Duration.ofSeconds(10))
                    .build()

            return try {
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                response.body()
            } catch (e: IOException) {
                log.warn("Error fetching content from {}", urlString, e)
                null
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                log.warn("Interrupted while fetching content from {}", urlString, e)
                null
            }
        }
    }
}
