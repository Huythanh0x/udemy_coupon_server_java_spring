package com.huythanh0x.udemycoupons.crawler_runner.fetcher;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebContentFetcher class.
 */
@ExtendWith(MockitoExtension.class)
class WebContentFetcherTest {

    private WebContentFetcher webContentFetcher;

    @BeforeEach
    void setUp() {
        webContentFetcher = new WebContentFetcher();
    }

    // Note: Network-dependent tests are skipped as they require actual network access
    // and can be slow/timeout. These should be tested in integration tests instead.
}

