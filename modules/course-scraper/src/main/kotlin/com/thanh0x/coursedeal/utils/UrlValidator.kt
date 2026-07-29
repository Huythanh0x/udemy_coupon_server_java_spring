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
        return try {
            if (urlString.isNullOrBlank()) return false

            val uri = URI.create(urlString)
            val host = uri.host

            if (host.isNullOrBlank()) {
                return false
            }

            // 1. Check Allowlist
            val isAllowed =
                ALLOWED_DOMAINS.any { domain ->
                    host.equals(domain, ignoreCase = true) || host.lowercase().endsWith(".$domain")
                }

            if (isAllowed) {
                return true
            }

            // 2. Block direct IP access or non-whitelisted domains
            if (isInternalIP(host)) {
                log.warn("SSRF Blocked: Attempt to access internal/private IP: {}", host)
                return false
            }

            log.warn("SSRF Blocked: Domain not in allowlist: {}", host)
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun isInternalIP(host: String): Boolean {
        return try {
            // If it's not a whitelisted domain, we check if it resolves to a private IP
            val address = InetAddress.getByName(host)
            address.isLoopbackAddress || address.isSiteLocalAddress || address.isLinkLocalAddress
        } catch (e: UnknownHostException) {
            // If it can't be resolved and isn't whitelisted, it's unsafe
            true
        }
    }
}
