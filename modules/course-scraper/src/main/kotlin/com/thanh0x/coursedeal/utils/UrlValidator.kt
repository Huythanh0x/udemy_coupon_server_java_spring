package com.thanh0x.coursedeal.utils

import com.thanh0x.coursedeal.config.logger
import java.net.InetAddress
import java.net.URI
import java.net.UnknownHostException

/**
 * Utility object to prevent Server-Side Request Forgery (SSRF) attacks.
 */
object UrlValidator {
    private val log = logger()

    private val ALLOWED_DOMAINS =
        setOf(
            "udemy.com",
            "www.udemy.com",
            "jobs.e-next.in",
            "cdn.real.discount",
        )

    /**
     * Validates a URL to prevent SSRF.
     */
    @JvmStatic
    fun isSafeUrl(urlString: String?): Boolean {
        if (urlString.isNullOrBlank()) return false

        return try {
            val host = URI.create(urlString).host
            if (host.isNullOrBlank()) false else (isAllowedDomain(host) || isSafeExternalHost(host))
        } catch (e: IllegalArgumentException) {
            log.warn("SSRF Blocked: Malformed URL {}: {}", urlString, e.message)
            false
        }
    }

    private fun isAllowedDomain(host: String): Boolean =
        ALLOWED_DOMAINS.any { domain ->
            host.equals(domain, ignoreCase = true) || host.lowercase().endsWith(".$domain")
        }

    /**
     * Blocks direct IP access or non-whitelisted domains. Always unsafe by definition -
     * only used once [isAllowedDomain] has already returned false.
     */
    private fun isSafeExternalHost(host: String): Boolean {
        if (isInternalIP(host)) {
            log.warn("SSRF Blocked: Attempt to access internal/private IP: {}", host)
            return false
        }
        log.warn("SSRF Blocked: Domain not in allowlist: {}", host)
        return false
    }

    private fun isInternalIP(host: String): Boolean {
        return try {
            // If it's not a whitelisted domain, we check if it resolves to a private IP
            val address = InetAddress.getByName(host)
            address.isLoopbackAddress || address.isSiteLocalAddress || address.isLinkLocalAddress
        } catch (e: UnknownHostException) {
            // If it can't be resolved and isn't whitelisted, it's unsafe
            log.warn("SSRF check: could not resolve host {}: {}", host, e.message)
            true
        }
    }
}
