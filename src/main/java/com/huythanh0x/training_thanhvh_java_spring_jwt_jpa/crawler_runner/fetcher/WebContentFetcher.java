package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.fetcher;

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

public class WebContentFetcher {

    public static JSONObject getJsonObjectFrom(String urlString) {
        return new JSONObject(getRawHTMLContentFrom(urlString));
    }

    public JSONArray getJsonArrayFrom(String urlString) {
        String rawHtml = getRawHTMLContentFrom(urlString);
        return new JSONArray(rawHtml);
    }

    public Document getHtmlDocumentFrom(String urlString) {
        return Jsoup.parse(getRawHTMLContentFrom(urlString));
    }

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
