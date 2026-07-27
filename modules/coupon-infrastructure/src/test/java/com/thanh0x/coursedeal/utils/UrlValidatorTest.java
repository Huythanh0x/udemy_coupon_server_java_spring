package com.thanh0x.coursedeal.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrlValidatorTest {

    @Test
    void testSafeUrls() {
        System.out.println("Testing safe URLs...");
        assertTrue(UrlValidator.isSafeUrl("https://www.udemy.com/course/python/"), "Failed: www.udemy.com");
        assertTrue(UrlValidator.isSafeUrl("https://udemy.com/course/java"), "Failed: udemy.com");
        assertTrue(UrlValidator.isSafeUrl("https://jobs.e-next.in/course/123"), "Failed: e-next.in");
        assertTrue(UrlValidator.isSafeUrl("https://cdn.real.discount/api/courses"), "Failed: real.discount");
    }

    @Test
    void testInternalUrls() {
        System.out.println("Testing internal URLs...");
        assertFalse(UrlValidator.isSafeUrl("http://localhost:8080"), "Failed: localhost");
        assertFalse(UrlValidator.isSafeUrl("http://127.0.0.1"), "Failed: 127.0.0.1");
        assertFalse(UrlValidator.isSafeUrl("http://192.168.1.1/admin"), "Failed: 192.168.1.1");
    }

    @Test
    void testUnsafeDomains() {
        System.out.println("Testing unsafe domains...");
        assertFalse(UrlValidator.isSafeUrl("https://google.com"), "Failed: google.com");
        assertFalse(UrlValidator.isSafeUrl("https://attacker.com"), "Failed: attacker.com");
    }
}
