package com.thanh0x.coursedeal.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * Utility class to prevent Server-Side Request Forgery (SSRF) attacks.
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
     */
    public static boolean isSafeUrl(String urlString) {
        try {
            if (urlString == null || urlString.isBlank()) return false;
            
            URI uri = URI.create(urlString);
            String host = uri.getHost();

            if (host == null || host.isBlank()) {
                return false;
            }

            // 1. Check Allowlist
            boolean isAllowed = ALLOWED_DOMAINS.stream()
                    .anyMatch(domain -> host.equalsIgnoreCase(domain) || host.toLowerCase().endsWith("." + domain));

            if (isAllowed) {
                return true;
            }

            // 2. Block direct IP access or non-whitelisted domains
            if (isInternalIP(host)) {
                log.warn("SSRF Blocked: Attempt to access internal/private IP: {}", host);
                return false;
            }

            log.warn("SSRF Blocked: Domain not in allowlist: {}", host);
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isInternalIP(String host) {
        try {
            // If it's not a whitelisted domain, we check if it resolves to a private IP
            InetAddress address = InetAddress.getByName(host);
            return address.isLoopbackAddress() || address.isSiteLocalAddress() || address.isLinkLocalAddress();
        } catch (UnknownHostException e) {
            // If it can't be resolved and isn't whitelisted, it's unsafe
            return true;
        }
    }
}
