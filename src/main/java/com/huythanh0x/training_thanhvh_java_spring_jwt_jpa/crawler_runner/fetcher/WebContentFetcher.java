package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.fetcher;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

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

    static String getRawHTMLContentFrom(String urlString) {
        var command = new String[]{"curl", "-g", urlString};
        var processBuilder = new ProcessBuilder(command);
        try {
            Process process = processBuilder.start();

            var inputStream = process.getInputStream();
            var reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder allContent = new StringBuilder();
            String newLine = reader.readLine();
            while (newLine != null) {
                allContent.append(newLine);
                newLine = reader.readLine();
            }
            process.waitFor(5, TimeUnit.SECONDS);
            return allContent.toString();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
