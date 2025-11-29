package com.huythanh0x.udemycoupons.e2e;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests that send actual HTTP requests to the running server.
 * These tests require the server to be running and accessible.
 * 
 * Set environment variable SERVER_BASE_URL to your server domain (e.g., https://api.example.com)
 * If not set, defaults to http://localhost:8080
 */
@SpringBootTest
@ActiveProfiles("test")
class ServerHealthCheckTest {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String SERVER_BASE_URL = System.getenv().getOrDefault("SERVER_BASE_URL", DEFAULT_BASE_URL);

    @Test
    @EnabledIfEnvironmentVariable(named = "ENABLE_E2E_TESTS", matches = "true")
    void testHealthEndpoint_WhenServerIsRunning_ReturnsOk() throws Exception {
        // Arrange
        URL url = new URI(SERVER_BASE_URL + "/actuator/health").toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        // Act
        int responseCode = connection.getResponseCode();

        // Assert
        assertEquals(200, responseCode, "Health endpoint should return 200 OK");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ENABLE_E2E_TESTS", matches = "true")
    void testApiCouponsEndpoint_WhenServerIsRunning_ReturnsOk() throws Exception {
        // Arrange
        URL url = new URI(SERVER_BASE_URL + "/api/v1/coupons?pageIndex=0&numberPerPage=10").toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        // Act
        int responseCode = connection.getResponseCode();

        // Assert
        assertTrue(responseCode == 200 || responseCode == 401, 
                   "Coupons endpoint should return 200 (if public) or 401 (if requires auth). Got: " + responseCode);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ENABLE_E2E_TESTS", matches = "true")
    void testSwaggerUiEndpoint_WhenServerIsRunning_ReturnsOk() throws Exception {
        // Arrange
        URL url = new URI(SERVER_BASE_URL + "/swagger-ui/index.html").toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        // Act
        int responseCode = connection.getResponseCode();

        // Assert
        assertTrue(responseCode == 200 || responseCode == 302, 
                   "Swagger UI should return 200 or redirect. Got: " + responseCode);
    }

    @Test
    void testServerBaseUrl_IsConfigured() {
        // This test always runs to verify configuration
        assertNotNull(SERVER_BASE_URL, "Server base URL should be configured");
        assertFalse(SERVER_BASE_URL.isEmpty(), "Server base URL should not be empty");
        System.out.println("Server Base URL configured as: " + SERVER_BASE_URL);
    }
}

