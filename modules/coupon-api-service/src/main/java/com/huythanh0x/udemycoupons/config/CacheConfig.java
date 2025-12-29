package com.huythanh0x.udemycoupons.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Cache configuration for Spring Cache abstraction.
 * Uses Redis as the cache backend with different TTLs for different cache types.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Creates a clean Jackson ObjectMapper for REST API responses.
     * This is the primary ObjectMapper used by Spring for JSON serialization.
     * Does NOT include type information to keep API responses clean.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // Do NOT enable default typing - this is for REST API responses
        return mapper;
    }
    
    /**
     * Creates a Jackson ObjectMapper configured specifically for Redis serialization.
     * This includes type information needed for GenericJackson2JsonRedisSerializer.
     * This is NOT used for REST API responses.
     */
    @Bean(name = "redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // Enable default typing for GenericJackson2JsonRedisSerializer to work properly
        mapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL
        );
        return mapper;
    }
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        // Use the specifically named redisObjectMapper bean (with type information for Redis)
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(12)) // Default TTL: 12 hours
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();
        
        // Course details cache: 24 hours
        RedisCacheConfiguration courseDetailsConfig = defaultConfig.entryTtl(Duration.ofHours(24));
        
        // Course reviews cache: 6 hours (more dynamic)
        RedisCacheConfiguration courseReviewsConfig = defaultConfig.entryTtl(Duration.ofHours(6));
        
        // Course curriculum cache: 24 hours (rarely changes)
        RedisCacheConfiguration courseCurriculumConfig = defaultConfig.entryTtl(Duration.ofHours(24));
        
        // Related courses cache: 12 hours
        RedisCacheConfiguration relatedCoursesConfig = defaultConfig.entryTtl(Duration.ofHours(12));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("courseDetails", courseDetailsConfig)
                .withCacheConfiguration("courseReviews", courseReviewsConfig)
                .withCacheConfiguration("courseCurriculum", courseCurriculumConfig)
                .withCacheConfiguration("relatedCourses", relatedCoursesConfig)
                .build();
    }
}

