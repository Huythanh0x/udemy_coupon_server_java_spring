package com.huythanh0x.udemycoupons.crawler_runner.fetcher;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * A utility class for fetching and parsing web content.
 */
public class WebContentFetcher {

    /**
     * Retrieves a JSONObject from a given URL by reading the raw HTML content and converting it into a JSONObject.
     *
     * @param urlString the URL from which to retrieve the JSON object
     * @return the JSONObject obtained from the URL, or null if the request failed or timed out
     */
    public static JSONObject getJsonObjectFrom(String urlString) {
        String content = getRawHTMLContentFrom(urlString);
        if (content == null || content.trim().isEmpty()) {
            System.out.println("Warning: Failed to fetch content from " + urlString + ", returning null");
            return null;
        }
        try {
            return new JSONObject(content);
        } catch (Exception e) {
            System.out.println("Error parsing JSON from " + urlString + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves raw HTML content from a specified URL and converts it into a JSONArray object.
     *
     * @param urlString the URL from which to retrieve the raw HTML content
     * @return a JSONArray object containing the parsed raw HTML content, or null if the request failed
     */
    public JSONArray getJsonArrayFrom(String urlString) {
        String rawHtml = getRawHTMLContentFrom(urlString);
        if (rawHtml == null || rawHtml.trim().isEmpty()) {
            System.out.println("Warning: Failed to fetch content from " + urlString + ", returning null");
            return null;
        }
        try {
            return new JSONArray(rawHtml);
        } catch (Exception e) {
            System.out.println("Error parsing JSON array from " + urlString + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the raw HTML content from a specified URL and parses it into an HTML Document using Jsoup.
     *
     * @param urlString the URL of the webpage to retrieve HTML content from
     * @return an HTML Document parsed from the raw HTML content of the specified URL, or null if the request failed
     */
    public Document getHtmlDocumentFrom(String urlString) {
        String rawHtml = getRawHTMLContentFrom(urlString);
        if (rawHtml == null || rawHtml.trim().isEmpty()) {
            System.out.println("Warning: Failed to fetch content from " + urlString + ", returning null");
            return null;
        }
        try {
            return Jsoup.parse(rawHtml);
        } catch (Exception e) {
            System.out.println("Error parsing HTML from " + urlString + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetches the raw HTML content from a given URL using the curl command.
     *
     * @param urlString the URL from which to fetch the raw HTML content
     * @return the raw HTML content retrieved from the provided URL as a String
     */
    public static String getRawHTMLContentFrom(String urlString) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .timeout(Duration.ofSeconds(10)) // Set a timeout for the request
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
            System.out.println(e);
        }
        return null;
    }
}
