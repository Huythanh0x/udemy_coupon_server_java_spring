package com.thanh0x.coursedeal.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

/**
 * Cache configuration for Spring Cache abstraction.
 * Uses Redis as the cache backend with different TTLs for different cache types.
 */
@Configuration
@EnableCaching
class CacheConfig {
    companion object {
        private const val DEFAULT_TTL_HOURS = 12L
        private const val COURSE_DETAILS_TTL_HOURS = 24L
        private const val COURSE_REVIEWS_TTL_HOURS = 6L
        private const val COURSE_CURRICULUM_TTL_HOURS = 24L
        private const val RELATED_COURSES_TTL_HOURS = 12L
    }

    /**
     * Creates a clean Jackson ObjectMapper for REST API responses.
     * This is the primary ObjectMapper used by Spring for JSON serialization.
     * Does NOT include type information to keep API responses clean.
     */
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerKotlinModule()
        }
    }

    /**
     * Creates a Jackson ObjectMapper configured specifically for Redis serialization.
     * This includes type information needed for GenericJackson2JsonRedisSerializer.
     * This is NOT used for REST API responses.
     */
    @Bean(name = ["redisObjectMapper"])
    fun redisObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(JavaTimeModule())
            registerKotlinModule()
            // Enable default typing for GenericJackson2JsonRedisSerializer to work properly
            activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
            )
        }
    }

    @Bean
    fun cacheManager(
        connectionFactory: RedisConnectionFactory,
        @Qualifier("redisObjectMapper") redisObjectMapper: ObjectMapper,
    ): CacheManager {
        // Use the specifically named redisObjectMapper bean (with type information for Redis)
        val jsonSerializer = GenericJackson2JsonRedisSerializer(redisObjectMapper)

        // Default cache configuration
        val defaultConfig =
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(DEFAULT_TTL_HOURS))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues()

        // Course details cache: rarely changes
        val courseDetailsConfig = defaultConfig.entryTtl(Duration.ofHours(COURSE_DETAILS_TTL_HOURS))

        // Course reviews cache: more dynamic
        val courseReviewsConfig = defaultConfig.entryTtl(Duration.ofHours(COURSE_REVIEWS_TTL_HOURS))

        // Course curriculum cache: rarely changes
        val courseCurriculumConfig = defaultConfig.entryTtl(Duration.ofHours(COURSE_CURRICULUM_TTL_HOURS))

        // Related courses cache
        val relatedCoursesConfig = defaultConfig.entryTtl(Duration.ofHours(RELATED_COURSES_TTL_HOURS))

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration("courseDetails", courseDetailsConfig)
            .withCacheConfiguration("courseReviews", courseReviewsConfig)
            .withCacheConfiguration("courseCurriculum", courseCurriculumConfig)
            .withCacheConfiguration("relatedCourses", relatedCoursesConfig)
            .build()
    }
}
