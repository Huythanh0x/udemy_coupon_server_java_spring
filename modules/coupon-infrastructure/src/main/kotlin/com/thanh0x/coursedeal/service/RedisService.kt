package com.thanh0x.coursedeal.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Service wrapper for Redis operations.
 * Provides simplified methods for common Redis operations like get, set, delete, and expire.
 */
class RedisService(private val redisTemplate: RedisTemplate<String, Any>) {
    private val valueOperations: ValueOperations<String, Any> = redisTemplate.opsForValue()

    /**
     * Sets a value in Redis with the given key.
     *
     * @param key   the Redis key
     * @param value the value to store
     */
    fun set(
        key: String,
        value: Any,
    ) {
        valueOperations.set(key, value)
    }

    /**
     * Sets a value in Redis with expiration time.
     *
     * @param key      the Redis key
     * @param value    the value to store
     * @param timeout  the expiration time
     * @param unit     the time unit for expiration
     */
    fun set(
        key: String,
        value: Any,
        timeout: Long,
        unit: TimeUnit,
    ) {
        valueOperations.set(key, value, timeout, unit)
    }

    /**
     * Sets a value in Redis with expiration duration.
     *
     * @param key       the Redis key
     * @param value     the value to store
     * @param duration  the expiration duration
     */
    fun set(
        key: String,
        value: Any,
        duration: Duration,
    ) {
        valueOperations.set(key, value, duration)
    }

    /**
     * Gets a value from Redis by key.
     *
     * @param key the Redis key
     * @return the value associated with the key, or null if not found
     */
    fun get(key: String): Any? {
        return valueOperations.get(key)
    }

    /**
     * Gets a value from Redis and casts it to the specified type.
     *
     * @param key   the Redis key
     * @param clazz the class to cast to
     * @param <T>   the type of the value
     * @return the value cast to the specified type, or null if not found
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(
        key: String,
        clazz: Class<T>,
    ): T? {
        val value = valueOperations.get(key) ?: return null
        if (clazz.isInstance(value)) {
            return value as T
        }
        throw ClassCastException("Cannot cast \${value.javaClass.name} to \${clazz.name}")
    }

    /**
     * Gets a string value from Redis.
     *
     * @param key the Redis key
     * @return the string value, or null if not found
     */
    fun getString(key: String): String? {
        val value = get(key)
        return value?.toString()
    }

    /**
     * Gets a Long value from Redis.
     *
     * @param key the Redis key
     * @return the Long value, or null if not found
     */
    fun getLong(key: String): Long? {
        val value = get(key) ?: return null
        if (value is Long) {
            return value
        }
        if (value is Number) {
            return value.toLong()
        }
        return try {
            value.toString().toLong()
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * Deletes a key from Redis.
     *
     * @param key the Redis key to delete
     * @return true if the key was deleted, false if it didn't exist
     */
    fun delete(key: String): Boolean? {
        return redisTemplate.delete(key)
    }

    /**
     * Checks if a key exists in Redis.
     *
     * @param key the Redis key
     * @return true if the key exists, false otherwise
     */
    fun hasKey(key: String): Boolean? {
        return redisTemplate.hasKey(key)
    }

    /**
     * Sets the expiration time for a key.
     *
     * @param key     the Redis key
     * @param timeout the expiration time
     * @param unit    the time unit
     * @return true if the expiration was set, false if the key doesn't exist
     */
    fun expire(
        key: String,
        timeout: Long,
        unit: TimeUnit,
    ): Boolean? {
        return redisTemplate.expire(key, timeout, unit)
    }

    /**
     * Gets the time-to-live (TTL) of a key in seconds.
     *
     * @param key the Redis key
     * @return the TTL in seconds, -1 if the key exists but has no expiration, -2 if the key doesn't exist
     */
    fun getExpire(key: String): Long? {
        return redisTemplate.getExpire(key)
    }

    companion object {
        @JvmStatic
        lateinit var instance: RedisService
    }
}
