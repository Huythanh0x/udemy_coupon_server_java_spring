package com.thanh0x.coursedeal.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * Utility class to prevent Server-Side Request Forgery (SSRF) attacks.
 * It ensures that the application only makes requests to allowed domains and blocks internal IP ranges.
 */
public class UrlValidator {
    private static final Logger log = LoggerFactory.getLogger(UrlValidator.class);

    private static final Set<String> ALLOWED_DOMAINS = Set.of(
            "udemy.com",
            "www.udemy.com",
            "jobs.e-next.in",
            "cdn.real.discount"
    );

    /**
     * Validates a URL to prevent SSRF.
     *
     * @param urlString The URL to validate.
     * @return true if the URL is safe, false otherwise.
     */
    public static boolean isSafeUrl(String urlString) {
        try {
            URI uri = URI.create(urlString);
            String host = uri.getHost();

            if (host == null || host.isBlank()) {
                log.warn("SSRF Blocked: URL has no host: {}", urlString);
                return false;
            }

            // 1. Check Allowlist
            boolean isAllowed = ALLOWED_DOMAINS.stream()
                    .anyMatch(domain -> host.equals(domain) || host.endsWith("." + domain));

            if (!isAllowed) {
                log.warn("SSRF Blocked: Domain not in allowlist: {}", host);
                return false;
            }

            // 2. Check for Private IP Ranges
            InetAddress address = InetAddress.getByName(host);
            if (address.isLoopbackAddress() || address.isSiteLocalAddress() || address.isLinkLocalAddress()) {
                log.warn("SSRF Blocked: Attempt to access internal/private IP: {} ({})", host, address.getHostAddress());
                return false;
            }

            return true;
        } catch (UnknownHostException e) {
            log.warn("SSRF Blocked: Unable to resolve host: {}", urlString);
            return false;
        } catch (Exception e) {
            log.warn("SSRF Blocked: Invalid URL format: {}", urlString);
            return false;
        }
    }
}
