package com.thanh0x.coursedeal.utils

import com.thanh0x.coursedeal.service.RedisService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * A utility object for managing the last fetched time using Redis.
 * Stores time as epoch milliseconds for consistency and performance.
 */
object LastFetchTimeManager {
    private val log: Logger = LoggerFactory.getLogger(LastFetchTimeManager::class.java)

    private const val REDIS_KEY = Constant.REDIS_KEY_LAST_FETCH_TIME

    /**
     * Saves the current time to Redis as epoch milliseconds.
     * Falls back gracefully if Redis is not available.
     */
    @JvmStatic
    fun updateLastBulkRefreshCoupon() {
        try {
            val redisService = RedisService.instance
            val epochMillis = System.currentTimeMillis()
            redisService.set(REDIS_KEY, epochMillis.toString())
            log.info(
                "Last fetch time saved to Redis: {} ({})",
                epochMillis, LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
            )
        } catch (e: IllegalStateException) {
            // Redis not available
            log.warn("Redis not available, cannot save fetch time: {}", e.message)
        }
    }

    /**
     * Reads the last fetched time in milliseconds from Redis.
     * Returns the time in milliseconds since epoch.
     * If Redis is not available or the key doesn't exist, returns the minimum value of a long.
     *
     * @return the last fetched time in milliseconds, or Long.MIN_VALUE if not available
     */
    @JvmStatic
    fun loadLasFetchedTimeInMilliSecond(): Long {
        return try {
            val redisService = RedisService.instance
            val epochMillisString = redisService.getString(REDIS_KEY)

            if (epochMillisString.isNullOrEmpty()) {
                Int.MIN_VALUE.toLong()
            } else {
                epochMillisString.toLong()
            }
        } catch (e: IllegalStateException) {
            // Redis not available
            Int.MIN_VALUE.toLong()
        } catch (e: NumberFormatException) {
            log.error("Error parsing last fetch time from Redis (expected epoch milliseconds): {}", e.message)
            Int.MIN_VALUE.toLong()
        } catch (e: Exception) {
            // Other errors
            log.error("Error reading last fetch time from Redis: {}", e.message)
            Int.MIN_VALUE.toLong()
        }
    }
}
