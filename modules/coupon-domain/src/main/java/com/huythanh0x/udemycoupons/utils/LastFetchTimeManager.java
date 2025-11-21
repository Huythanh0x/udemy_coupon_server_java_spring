package com.huythanh0x.udemycoupons.utils;

import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * A utility class for managing the last fetched time.
 */
public class LastFetchTimeManager {
    /**
     * Generates a JSON file containing the current local time in ISO format and saves it to a specified file path.
     */
    public static void dumpFetchedTimeJsonToFile() {
        String jsonFilePath = "fetched_time.json";
        var resultJson = new JSONObject();
        resultJson.put("localTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        try (FileWriter fileWriter = new FileWriter(jsonFilePath)) {
            fileWriter.write(resultJson.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the last fetched time in milliseconds from a file named "fetched_time.json",
     * parses the value from the JSON object, and converts it to the milliseconds since epoch.
     * If an IO exception occurs during file reading, returns the minimum value of a long.
     *
     * @return the last fetched time in milliseconds
     */
    public static Long loadLasFetchedTimeInMilliSecond() {
        try {
            String couponsJson = new String(Files.readAllBytes(Paths.get("fetched_time.json")));
            var responseJsonObject = new JSONObject(couponsJson);
            var dateTimeString = responseJsonObject.getString("localTime");
            var formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            var localDateTime = LocalDateTime.parse(dateTimeString, formatter);
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (IOException e) {
            return (long) Integer.MIN_VALUE;
        }
    }

    /**
     * Reads the last fetched time from a JSON file and converts it to a LocalDateTime object
     *
     * @return the LocalDateTime object representing the last fetched time or null if an IOException occurs
     */
    public static LocalDateTime loadLasFetchedTimeInDateTimeString() {
        try {
            String couponsJson = new String(Files.readAllBytes(Paths.get("fetched_time.json")));
            var responseJsonObject = new JSONObject(couponsJson);
            var dateTimeString = responseJsonObject.getString("localTime");
            var formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            return LocalDateTime.parse(dateTimeString, formatter);
        } catch (IOException e) {
            return null;
        }
    }
}
