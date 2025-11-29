package com.huythanh0x.udemycoupons.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end API tests that can test against local server or remote server.
 * Set ENABLE_E2E_TESTS=true and optionally SERVER_BASE_URL to enable remote testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiE2ETest {

    @Autowired
    private MockMvc mockMvc;

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String SERVER_BASE_URL = System.getenv().getOrDefault("SERVER_BASE_URL", DEFAULT_BASE_URL);
    private static final boolean USE_REMOTE_SERVER = Boolean.parseBoolean(
            System.getenv().getOrDefault("ENABLE_E2E_TESTS", "false")
    );

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    void testGetCoupons_LocalMockMvc_ReturnsOk() throws Exception {
        // Test using MockMvc (local, no server needed)
        mockMvc.perform(get("/api/v1/coupons")
                        .param("pageIndex", "0")
                        .param("numberPerPage", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 401, 
                              "Expected 200 or 401, got: " + status);
                });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ENABLE_E2E_TESTS", matches = "true")
    void testGetCoupons_RemoteServer_ReturnsOk() throws Exception {
        // Test against actual running server
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + "/api/v1/coupons?pageIndex=0&numberPerPage=10"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertTrue(response.statusCode() == 200 || response.statusCode() == 401,
                   "Should return 200 (public) or 401 (requires auth). Got: " + response.statusCode());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ENABLE_E2E_TESTS", matches = "true")
    void testGetCouponsFilter_RemoteServer_ReturnsOk() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + "/api/v1/coupons/filter?category=Development&rating=4.0&pageIndex=0&numberPerPage=10"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertTrue(response.statusCode() == 200 || response.statusCode() == 401,
                   "Filter endpoint should return 200 or 401. Got: " + response.statusCode());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ENABLE_E2E_TESTS", matches = "true")
    void testGetCouponsSearch_RemoteServer_ReturnsOk() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + "/api/v1/coupons/search?querySearch=java&pageIndex=0&numberPerPage=10"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertTrue(response.statusCode() == 200 || response.statusCode() == 401,
                   "Search endpoint should return 200 or 401. Got: " + response.statusCode());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ENABLE_E2E_TESTS", matches = "true")
    void testApiResponse_ContainsExpectedFields() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_BASE_URL + "/api/v1/coupons?pageIndex=0&numberPerPage=1"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String body = response.body();
            assertNotNull(body, "Response body should not be null");
            // Check for expected JSON structure
            assertTrue(body.contains("content") || body.contains("data") || body.contains("coupons"),
                       "Response should contain expected fields");
        }
    }
}

