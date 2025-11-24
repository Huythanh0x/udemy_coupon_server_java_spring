package com.huythanh0x.udemycoupons.utils;

import com.huythanh0x.udemycoupons.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * A utility class for managing the last fetched time using Redis.
 * Stores time as epoch milliseconds for consistency and performance.
 */
public class LastFetchTimeManager {
    private static final Logger log = LoggerFactory.getLogger(LastFetchTimeManager.class);
    
    private static final String REDIS_KEY = Constant.REDIS_KEY_LAST_FETCH_TIME;
    
    /**
     * Saves the current time to Redis as epoch milliseconds.
     * Falls back gracefully if Redis is not available.
     */
    public static void updateLastBulkRefreshCoupon() {
        try {
            RedisService redisService = RedisService.getInstance();
            long epochMillis = System.currentTimeMillis();
            redisService.set(REDIS_KEY, String.valueOf(epochMillis));
            log.info("Last fetch time saved to Redis: {} ({})",
                    epochMillis, LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault()));
        } catch (IllegalStateException e) {
            // Redis not available
            log.warn("Redis not available, cannot save fetch time: {}", e.getMessage());
        }
    }

    /**
     * Reads the last fetched time in milliseconds from Redis.
     * Returns the time in milliseconds since epoch.
     * If Redis is not available or the key doesn't exist, returns the minimum value of a long.
     *
     * @return the last fetched time in milliseconds, or Long.MIN_VALUE if not available
     */
    public static Long loadLasFetchedTimeInMilliSecond() {
        try {
            RedisService redisService = RedisService.getInstance();
            String epochMillisString = redisService.getString(REDIS_KEY);
            
            if (epochMillisString == null || epochMillisString.isEmpty()) {
                return (long) Integer.MIN_VALUE;
            }
            
            return Long.parseLong(epochMillisString);
        } catch (IllegalStateException e) {
            // Redis not available
            return (long) Integer.MIN_VALUE;
        } catch (NumberFormatException e) {
            log.error("Error parsing last fetch time from Redis (expected epoch milliseconds): {}", e.getMessage());
            return (long) Integer.MIN_VALUE;
        } catch (Exception e) {
            // Other errors
            log.error("Error reading last fetch time from Redis: {}", e.getMessage());
            return (long) Integer.MIN_VALUE;
        }
    }
}
