package com.huythanh0x.udemycoupons.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service wrapper for Redis operations.
 * Provides simplified methods for common Redis operations like get, set, delete, and expire.
 */
public class RedisService {
    
    private static RedisService instance;
    
    /**
     * Set the singleton instance (used for static access from LastFetchTimeManager).
     * Should be called during Spring bean initialization.
     *
     * @param redisService the RedisService instance
     */
    public static void setInstance(RedisService redisService) {
        instance = redisService;
    }
    
    /**
     * Get the singleton instance.
     *
     * @return the RedisService instance
     * @throws IllegalStateException if instance is not set
     */
    public static RedisService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RedisService instance has not been initialized. Make sure Spring context is loaded.");
        }
        return instance;
    }
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOperations;
    
    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
    }
    
    /**
     * Sets a value in Redis with the given key.
     *
     * @param key   the Redis key
     * @param value the value to store
     */
    public void set(String key, Object value) {
        valueOperations.set(key, value);
    }
    
    /**
     * Sets a value in Redis with expiration time.
     *
     * @param key      the Redis key
     * @param value    the value to store
     * @param timeout  the expiration time
     * @param unit     the time unit for expiration
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        valueOperations.set(key, value, timeout, unit);
    }
    
    /**
     * Sets a value in Redis with expiration duration.
     *
     * @param key       the Redis key
     * @param value     the value to store
     * @param duration  the expiration duration
     */
    public void set(String key, Object value, Duration duration) {
        valueOperations.set(key, value, duration);
    }
    
    /**
     * Gets a value from Redis by key.
     *
     * @param key the Redis key
     * @return the value associated with the key, or null if not found
     */
    public Object get(String key) {
        return valueOperations.get(key);
    }
    
    /**
     * Gets a value from Redis and casts it to the specified type.
     *
     * @param key   the Redis key
     * @param clazz the class to cast to
     * @param <T>   the type of the value
     * @return the value cast to the specified type, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = valueOperations.get(key);
        if (value == null) {
            return null;
        }
        if (clazz.isInstance(value)) {
            return (T) value;
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getName() + " to " + clazz.getName());
    }
    
    /**
     * Gets a string value from Redis.
     *
     * @param key the Redis key
     * @return the string value, or null if not found
     */
    public String getString(String key) {
        Object value = get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Gets a Long value from Redis.
     *
     * @param key the Redis key
     * @return the Long value, or null if not found
     */
    public Long getLong(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Deletes a key from Redis.
     *
     * @param key the Redis key to delete
     * @return true if the key was deleted, false if it didn't exist
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
    
    /**
     * Checks if a key exists in Redis.
     *
     * @param key the Redis key
     * @return true if the key exists, false otherwise
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    
    /**
     * Sets the expiration time for a key.
     *
     * @param key     the Redis key
     * @param timeout the expiration time
     * @param unit    the time unit
     * @return true if the expiration was set, false if the key doesn't exist
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }
    
    /**
     * Gets the time-to-live (TTL) of a key in seconds.
     *
     * @param key the Redis key
     * @return the TTL in seconds, -1 if the key exists but has no expiration, -2 if the key doesn't exist
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }
}

